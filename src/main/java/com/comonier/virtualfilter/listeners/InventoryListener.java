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

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("Filter")) return;

        event.setCancelled(true);
        String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");

        int slot = event.getSlot();
        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();

        // --- 1. MOVER COM O MOUSE (CLIQUE ESQUERDO) ---
        if (event.getClick() == ClickType.LEFT && slot >= 0 && slot < 54) {
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                if (cursorItem.getType().getMaxStackSize() <= 1) return;
                
                int allowed = getMaxSlots(player, typeCode);
                if (slot >= allowed) {
                    player.sendMessage("§cSlot locked!");
                    return;
                }

                handleCreation(player, typeCode, cursorItem.getType(), slot);
                player.setItemOnCursor(null); 
                FilterMenu.open(player, typeCode);
                return;
            }
        }

        // --- 2. QUICK-ADD (SHIFT + ESQUERDO) ---
        if (event.getClickedInventory() == player.getInventory() && event.getClick() == ClickType.SHIFT_LEFT) {
            ItemStack toAdd = event.getCurrentItem();
            if (toAdd != null && toAdd.getType() != Material.AIR && toAdd.getType().getMaxStackSize() > 1) {
                handleCreation(player, typeCode, toAdd.getType(), -1);
                FilterMenu.open(player, typeCode);
            }
            return;
        }

        // --- 3. SAQUE ISF (DIREITO) ---
        if (event.getClick() == ClickType.RIGHT && typeCode.equals("isf") && slot >= 0) {
            if (clickedItem != null && !clickedItem.getType().name().contains("GLASS_PANE")) {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
                if (matName != null) {
                    VirtualFilter.getInstance().getInfinityManager().withdrawPack(player, matName, slot);
                    if (VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName) <= 0) {
                        executeDelete(player, typeCode, slot);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                    FilterMenu.open(player, typeCode);
                }
            }
            return;
        }

        // --- 4. DELETAR (SHIFT + DIREITO) ---
        if (event.getClick() == ClickType.SHIFT_RIGHT && clickedItem != null && !clickedItem.getType().name().contains("GLASS_PANE")) {
            executeDelete(player, typeCode, slot);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            FilterMenu.open(player, typeCode);
        }
    }

    private void handleCreation(Player player, String type, Material mat, int specificSlot) {
        // Verifica se o jogador já possui esse filtro em qualquer slot desse menu
        int existingSlot = -1;
        for (int i = 0; i < 54; i++) {
            String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
            if (m != null && m.equalsIgnoreCase(mat.name())) {
                existingSlot = i;
                break;
            }
        }

        // Se já existe e é ISF, vamos apenas somar ao estoque
        if (existingSlot != -1) {
            if (type.equals("isf")) {
                long toAdd = 0;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == mat) {
                        toAdd += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() == mat) {
                    toAdd += player.getItemOnCursor().getAmount();
                }
                VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), mat.name(), (int) toAdd);
                player.sendMessage("§aItems merged into existing " + mat.name() + " filter.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            } else {
                player.sendMessage("§cYou already have a filter for " + mat.name() + "!");
            }
            return;
        }

        // Se não existe, cria um novo no slot desejado ou no próximo livre
        int targetSlot = specificSlot;
        if (targetSlot == -1 || (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, targetSlot) != null)) {
            int allowed = getMaxSlots(player, type);
            targetSlot = -1; // Reset para busca
            for (int i = 0; i < allowed; i++) {
                if (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i) == null) {
                    targetSlot = i; break;
                }
            }
        }

        if (targetSlot != -1) {
            long total = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == mat) {
                        total += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() == mat) {
                    total += player.getItemOnCursor().getAmount();
                }
            }
            saveItem(player, type, targetSlot, mat.name(), total);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        } else {
            player.sendMessage("§cNo slots available!");
        }
    }

    private int getMaxSlots(Player player, String type) {
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        return Math.min(max, 54);
    }

    private void saveItem(Player p, String type, int slot, String mat, long amount) {
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat);
            ps.setLong(5, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executeDelete(Player p, String type, int slot) {
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
