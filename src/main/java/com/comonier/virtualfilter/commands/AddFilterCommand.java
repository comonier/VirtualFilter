package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

public class AddFilterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        final String type = label.toLowerCase().replace("add", "");
        final UUID uuid = player.getUniqueId();
        final String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        ItemStack hand = player.getInventory().getItemInMainHand();
        
        if (null == hand || hand.getType() == Material.AIR) {
            player.sendMessage("§c§lSYNTAX ERROR: §fSegure um item na mão!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        final Material matType = hand.getType();
        final String matName = matType.name();

        // NOVA TRAVA: Shulker Boxes sao proibidas em qualquer filtro
        if (matName.contains("SHULKER_BOX")) {
            player.sendMessage("§c§lERROR: §fShulker Boxes não podem ser filtradas!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        if (2 > matType.getMaxStackSize() || (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName())) {
            player.sendMessage("§c§lERROR: §fItem inválido.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        // --- LOGICA DE MERGE ---
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, type, matName)) {
            if (type.equals("isf")) {
                long totalAdded = 0;
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (null != invItem && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        totalAdded += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                if (totalAdded > 0) {
                    VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, totalAdded);
                    player.sendMessage("§6[VF] §f" + totalAdded + "x §e" + matName + " §7merged!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                }
            }
            return true;
        }

        int allowed = getMaxSlots(player, type);
        int targetSlotId = -1;

        for (int i = 0; allowed > i; i++) {
            String existing = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i);
            if (null == existing || existing.isEmpty() || existing.equalsIgnoreCase("AIR")) {
                targetSlotId = i;
                break;
            }
        }

        final int targetSlot = targetSlotId;

        if (-1 != targetSlot) {
            long initialAmount = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (null != invItem && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        initialAmount += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }

            final long finalAmount = initialAmount;
            saveToDB(uuid, type, targetSlot, matName, finalAmount);
            
            VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                player.updateInventory(); 
                FilterMenu.open(player, type);
                
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "added")
                        .replace("%item%", matName)
                        .replace("%type%", type.toUpperCase())
                        .replace("%slot%", String.valueOf(targetSlot + 1)));
                
                if (finalAmount > 0) {
                    player.sendMessage("§6[VF] §f" + finalAmount + "x §e" + matName + " §7stored!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
            }, 2L);
        }
        return true;
    }

    private int getMaxSlots(Player p, String type) {
        if (p.isOp() || p.hasPermission("virtualfilter.admin")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            String s = perm.getPermission().toLowerCase();
            if (s.startsWith("virtualfilter." + type + ".")) {
                try {
                    int v = Integer.parseInt(s.substring(s.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        return (max >= 54) ? 54 : max;
    }

    private void saveToDB(UUID uuid, String type, int slot, String mat, long amount) {
        String query = "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = VirtualFilter.getInstance().getDbCore().getConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot);
            ps.setString(4, mat.toUpperCase());
            ps.setLong(5, amount);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) { 
            e.printStackTrace(); 
        }
    }
}
