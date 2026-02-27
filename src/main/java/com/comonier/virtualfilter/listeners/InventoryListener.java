package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Correção de símbolo: Cast tradicional
        if (false == (event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (false == title.contains("Filter")) {
            return;
        }

        event.setCancelled(true);
        
        String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");
        boolean isISF = typeCode.equals("isf");

        int slot = event.getSlot();
        if (0 > slot || slot >= 54) {
            return;
        }

        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getClickedInventory() == player.getInventory()) {
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                ItemStack toAdd = event.getCurrentItem();
                if (null != toAdd && toAdd.getType() != Material.AIR && toAdd.getType().getMaxStackSize() > 1) {
                    handleCreation(player, typeCode, toAdd.getType(), -1);
                    FilterMenu.open(player, typeCode);
                }
            }
            return;
        }

        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
            if (event.getClick() == ClickType.SHIFT_LEFT && isISF && isFilterItem(clickedItem)) {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
                if (null != matName) {
                    VirtualFilter.getInstance().getInfinityManager().withdrawMassive(player, matName);
                    if (0 >= VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName)) {
                        executeDelete(player, typeCode, slot);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                    FilterMenu.open(player, typeCode);
                }
                return;
            }

            if (null != cursorItem && cursorItem.getType() != Material.AIR) {
                if (2 > cursorItem.getType().getMaxStackSize()) {
                    return;
                }
                int allowed = getMaxSlots(player, typeCode);
                if (slot >= allowed) {
                    player.sendMessage("§cSlot locked!");
                    return;
                }
                if (isFilterItem(clickedItem)) {
                    player.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));
                }
                handleCreation(player, typeCode, cursorItem.getType(), slot);
                player.setItemOnCursor(null);
                FilterMenu.open(player, typeCode);
                return;
            }
        }

        if (event.getClick() == ClickType.RIGHT && isISF && isFilterItem(clickedItem)) {
            String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
            if (null != matName) {
                VirtualFilter.getInstance().getInfinityManager().withdrawPack(player, matName, slot);
                if (0 >= VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName)) {
                    executeDelete(player, typeCode, slot);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                FilterMenu.open(player, typeCode);
            }
            return;
        }

        if (event.getClick() == ClickType.SHIFT_RIGHT && isFilterItem(clickedItem)) {
            if (isISF) {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
                if (null != matName) {
                    long currentStock = VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName);
                    if (currentStock > 0) {
                        player.sendMessage("§c§lAVISO: §fVocê não pode deletar um filtro com itens estocados!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                        return;
                    }
                }
            }
            executeDelete(player, typeCode, slot);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            FilterMenu.open(player, typeCode);
        }
    }

    private boolean isFilterItem(ItemStack item) {
        return null != item && item.getType() != Material.AIR && false == item.getType().name().contains("GLASS_PANE");
    }

    private void handleCreation(Player player, String type, Material mat, int specificSlot) {
        int existingSlot = -1;
        for (int i = 0; 54 > i; i++) {
            String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
            if (null != m && m.equalsIgnoreCase(mat.name())) {
                existingSlot = i;
                break;
            }
        }

        if (-1 != existingSlot) {
            if (type.equals("isf")) {
                long toAdd = 0;
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (null != invItem && invItem.getType() == mat) {
                        toAdd += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), mat.name(), toAdd);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            }
            return;
        }

        int targetSlot = specificSlot;
        if (-1 == targetSlot) {
            int allowed = getMaxSlots(player, type);
            for (int i = 0; allowed > i; i++) {
                if (null == VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i)) {
                    targetSlot = i; 
                    break;
                }
            }
        }

        if (-1 != targetSlot) {
            long total = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (null != invItem && invItem.getType() == mat) {
                        total += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
            saveItem(player, type, targetSlot, mat.name(), total);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        }
    }

    private int getMaxSlots(Player player, String type) {
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) {
            return 54;
        }
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            String prefix = "virtualfilter." + type + ".";
            if (perm.startsWith(prefix)) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (v > max) {
                        max = v;
                    }
                } catch (Exception ignored) {}
            }
        }
        if (max >= 54) return 54;
        return max;
    }

    private void saveItem(Player p, String type, int slot, String mat, long amount) {
        String query = "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat);
            ps.setLong(5, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executeDelete(Player p, String type, int slot) {
        String query = "DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
