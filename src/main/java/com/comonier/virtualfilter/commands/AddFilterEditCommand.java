package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterEditMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

public class AddFilterEditCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        final String type = "isfe";
        final UUID uuid = player.getUniqueId();
        final String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        ItemStack hand = player.getInventory().getItemInMainHand();
        
        if (null == hand || hand.getType() == Material.AIR) {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "syntax_error")
                    .replace("%cmd%", label).replace("%args%", "[slot]"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        final Material matType = hand.getType();
        final String matName = matType.name();
        final ItemMeta meta = hand.getItemMeta();

        if (2 > matType.getMaxStackSize() || matName.contains("SHULKER_BOX")) {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "invalid_item"));
            return true;
        }

        if (null == meta || false == meta.hasDisplayName()) {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "invalid_edit_item"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        final String customName = meta.getDisplayName();

        if (VirtualFilter.getInstance().getFilterEditRepo().hasFilter(uuid, type, matName, customName)) {
            long totalAdded = 0;
            for (ItemStack invItem : player.getInventory().getStorageContents()) {
                if (null != invItem && invItem.getType() == matType && invItem.hasItemMeta()) {
                    if (customName.equals(invItem.getItemMeta().getDisplayName())) {
                        totalAdded += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
            if (totalAdded > 0) {
                VirtualFilter.getInstance().getFilterEditRepo().addAmount(uuid, matName, customName, totalAdded);
                player.sendMessage("§6[VFE] §f" + totalAdded + "x §e" + customName + " §7merged!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            }
            return true;
        }

        int allowed = getMaxSlots(player, "isfe");
        int targetSlotId = -1;

        for (int i = 0; allowed > i; i++) {
            String[] existing = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, type, i);
            if (null == existing) {
                targetSlotId = i;
                break;
            }
        }

        if (-1 != targetSlotId) {
            byte[] itemBytes = hand.serializeAsBytes();
            long initialAmount = 0;
            for (ItemStack invItem : player.getInventory().getStorageContents()) {
                if (null != invItem && invItem.getType() == matType && invItem.hasItemMeta()) {
                    if (customName.equals(invItem.getItemMeta().getDisplayName())) {
                        initialAmount += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }

            saveToDB(uuid, type, targetSlotId, matName, customName, initialAmount, itemBytes);
            
            final int finalSlot = targetSlotId + 1;
            final long finalAmt = initialAmount;

            VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                player.updateInventory();
                FilterEditMenu.open(player);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "added")
                        .replace("%item%", customName)
                        .replace("%type%", "ISFE")
                        .replace("%slot%", String.valueOf(finalSlot)));
                
                if (finalAmt > 0) {
                    player.sendMessage("§6[VFE] §f" + finalAmt + "x §e" + customName + " §7stored!");
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
            }, 2L);
        }
        return true;
    }

    private int getMaxSlots(Player p, String type) {
        if (p.isOp() || p.hasPermission("virtualfilter.admin")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots.isf", 1);
        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            String s = perm.getPermission().toLowerCase();
            if (s.startsWith("virtualfilter.isfe.")) {
                try {
                    int v = Integer.parseInt(s.substring(s.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        return (max >= 54) ? 54 : max;
    }

    private void saveToDB(UUID uuid, String type, int slot, String mat, String customName, long amount, byte[] data) {
        String query = "INSERT OR REPLACE INTO player_filters_edit (uuid, filter_type, slot_id, material, custom_name, amount, item_data) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = VirtualFilter.getInstance().getDbCore().getEditConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat.toUpperCase());
            ps.setString(5, customName);
            ps.setLong(6, amount);
            ps.setBytes(7, data);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }
}
