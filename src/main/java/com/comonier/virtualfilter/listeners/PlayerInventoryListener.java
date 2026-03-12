package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import com.comonier.virtualfilter.menu.FilterEditMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.UUID;

public class PlayerInventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInvClick(InventoryClickEvent event) {
        if (false == (event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != player.getInventory()) return;
        
        String title = event.getView().getTitle();
        if (false == (title.contains("Filter") || title.contains("InfinityStackFilterEdited"))) return;

        if (event.getClick() == ClickType.SHIFT_LEFT) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            
            event.setCancelled(true);
            
            if (title.contains("InfinityStackFilterEdited")) {
                handleProcessEdit(player, clickedItem);
                VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                    player.updateInventory();
                    FilterEditMenu.open(player);
                }, 2L);
            } else {
                String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStackFilter") ? "isf" : "asf");
                
                if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                    String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(player.getUniqueId());
                    player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "invalid_item"));
                    return;
                }

                handleProcess(player, typeCode, clickedItem);
                VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                    player.updateInventory();
                    FilterMenu.open(player, typeCode);
                }, 2L);
            }
        }
    }

    private void handleProcessEdit(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        if (false == (item.hasItemMeta() && item.getItemMeta().hasDisplayName())) {
            String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "invalid_edit_item"));
            return;
        }

        Material matType = item.getType();
        String matName = matType.name();
        String customName = item.getItemMeta().getDisplayName();

        if (VirtualFilter.getInstance().getFilterEditRepo().hasFilter(uuid, "isfe", matName, customName)) {
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
            return;
        }

        int allowed = getMaxSlots(player, "isfe");
        int targetSlot = -1;
        for (int i = 0; allowed > i; i++) {
            String[] existing = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, "isfe", i);
            if (null == existing) {
                targetSlot = i;
                break;
            }
        }

        if (-1 != targetSlot) {
            byte[] itemData = item.serializeAsBytes();
            long total = 0;
            for (ItemStack invItem : player.getInventory().getStorageContents()) {
                if (null != invItem && invItem.getType() == matType && invItem.hasItemMeta()) {
                    if (customName.equals(invItem.getItemMeta().getDisplayName())) {
                        total += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
            saveToDBEdit(uuid, "isfe", targetSlot, matName, customName, total, itemData);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
        }
    }

    private void handleProcess(Player player, String type, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Material matType = item.getType();
        String matName = matType.name();
        
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, type, matName)) {
            if (type.equals("isf")) {
                long totalAdded = 0;
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        if (false == (invItem.hasItemMeta() && invItem.getItemMeta().hasDisplayName())) {
                            totalAdded += invItem.getAmount();
                            invItem.setAmount(0);
                        }
                    }
                }
                if (totalAdded > 0) {
                    VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, totalAdded);
                    player.sendMessage("§6[VF] §f" + totalAdded + "x §e" + matName + " §7merged!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                }
            }
            return;
        }
        int allowed = getMaxSlots(player, type);
        int targetSlot = -1;
        for (int i = 0; allowed > i; i++) {
            String existing = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i);
            if (null == existing || existing.isEmpty() || existing.equalsIgnoreCase("AIR")) {
                targetSlot = i; break;
            }
        }
        if (targetSlot != -1) {
            long total = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        if (false == (invItem.hasItemMeta() && invItem.getItemMeta().hasDisplayName())) {
                            total += invItem.getAmount();
                            invItem.setAmount(0);
                        }
                    }
                }
            }
            saveToDB(uuid, type, targetSlot, matName, total);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
        }
    }

    private int getMaxSlots(Player p, String type) {
        if (p.isOp() || p.hasPermission("virtualfilter.admin")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots.isf", 1);
        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            String s = perm.getPermission().toLowerCase();
            String prefix = "virtualfilter." + type + ".";
            if (s.startsWith(prefix)) {
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
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot); ps.setString(4, mat.toUpperCase());
            ps.setLong(5, amount); ps.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    private void saveToDBEdit(UUID uuid, String type, int slot, String mat, String custom, long amount, byte[] data) {
        String query = "INSERT OR REPLACE INTO player_filters_edit (uuid, filter_type, slot_id, material, custom_name, amount, item_data) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = VirtualFilter.getInstance().getDbCore().getEditConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot); ps.setString(4, mat.toUpperCase());
            ps.setString(5, custom); ps.setLong(6, amount);
            ps.setBytes(7, data); ps.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }
}
