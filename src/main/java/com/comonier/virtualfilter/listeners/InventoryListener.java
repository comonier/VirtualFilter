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

        // Cancela o padrão para evitar roubo de itens técnicos do menu
        event.setCancelled(true);
        
        String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");
        boolean isISF = typeCode.equals("isf");

        int slot = event.getSlot();
        if (slot < 0 || slot >= 54) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();

        // --- 1. QUICK-ADD (SHIFT + ESQUERDO no inv de baixo) ---
        if (event.getClickedInventory() == player.getInventory()) {
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                ItemStack toAdd = event.getCurrentItem();
                if (toAdd != null && toAdd.getType() != Material.AIR && toAdd.getType().getMaxStackSize() > 1) {
                    handleCreation(player, typeCode, toAdd.getType(), -1);
                    FilterMenu.open(player, typeCode);
                }
            }
            return;
        }

        // --- 2. CLIQUE NO MENU (PARTE DE CIMA) ---
        
        // ESQUERDO: Adicionar, Substituir ou SAQUE MASSIVO (ISF)
        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
            
            // CASO A: Saque Massivo (Shift + Clique Esquerdo em item existente no ISF)
            if (event.getClick() == ClickType.SHIFT_LEFT && isISF && isFilterItem(clickedItem)) {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
                if (matName != null) {
                    VirtualFilter.getInstance().getInfinityManager().withdrawMassive(player, matName);
                    // Se o estoque zerar após o saque massivo, remove o filtro
                    if (VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName) <= 0) {
                        executeDelete(player, typeCode, slot);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                    FilterMenu.open(player, typeCode);
                }
                return;
            }

            // CASO B: Adicionar/Substituir com item no mouse (Clique Esquerdo Normal)
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                if (cursorItem.getType().getMaxStackSize() <= 1) return;
                
                int allowed = getMaxSlots(player, typeCode);
                if (slot >= allowed) {
                    player.sendMessage("§cSlot locked!");
                    return;
                }

                // Devolve o item antigo limpo se estiver substituindo
                if (isFilterItem(clickedItem)) {
                    player.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));
                }

                handleCreation(player, typeCode, cursorItem.getType(), slot);
                player.setItemOnCursor(null); // Consome o item do mouse
                FilterMenu.open(player, typeCode);
                return;
            }
        }

        // DIREITO: Saque Simples (1 Pack no ISF)
        if (event.getClick() == ClickType.RIGHT && isISF && isFilterItem(clickedItem)) {
            String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
            if (matName != null) {
                VirtualFilter.getInstance().getInfinityManager().withdrawPack(player, matName, slot);
                if (VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName) <= 0) {
                    executeDelete(player, typeCode, slot);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                FilterMenu.open(player, typeCode);
            }
            return;
        }

        // SHIFT + DIREITO: Deletar Filtro (Universal)
        if (event.getClick() == ClickType.SHIFT_RIGHT && isFilterItem(clickedItem)) {
            executeDelete(player, typeCode, slot);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            FilterMenu.open(player, typeCode);
        }
    }

    private boolean isFilterItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR && !item.getType().name().contains("GLASS_PANE");
    }

    private void handleCreation(Player player, String type, Material mat, int specificSlot) {
        // Lógica de auto-mesclagem
        int existingSlot = -1;
        for (int i = 0; i < 54; i++) {
            String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
            if (m != null && m.equalsIgnoreCase(mat.name())) {
                existingSlot = i;
                break;
            }
        }

        if (existingSlot != -1) {
            if (type.equals("isf")) {
                long toAdd = 0;
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == mat) {
                        toAdd += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), mat.name(), (int) toAdd);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            }
            return;
        }

        int targetSlot = specificSlot;
        if (targetSlot == -1) {
            int allowed = getMaxSlots(player, type);
            for (int i = 0; i < allowed; i++) {
                if (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i) == null) {
                    targetSlot = i; break;
                }
            }
        }

        if (targetSlot != -1) {
            long total = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == mat) {
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
