package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        boolean isABF = title.contains("AutoBlockFilter");
        boolean isISF = title.contains("InfinityStackFilter");
        boolean isASF = title.contains("AutoSellFilter");

        if (isABF || isISF || isASF) {
            String typeCode = isABF ? "abf" : (isISF ? "isf" : "asf");

            // --- 1. Quick-Add: SHIFT + CLICK ESQUERDO no inventário de baixo ---
            if (event.getClickedInventory() == player.getInventory()) {
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    event.setCancelled(true);
                    ItemStack toAdd = event.getCurrentItem();
                    if (toAdd == null || toAdd.getType() == Material.AIR || toAdd.getType().getMaxStackSize() <= 1) return;

                    handleQuickAdd(player, typeCode, toAdd.getType());
                    // Atualiza o menu sem fechar
                    FilterMenu.open(player, typeCode);
                    return;
                }
                return; 
            }

            // --- 2. Lógica do Menu do Filtro (Parte de Cima) ---
            int slot = event.getSlot();
            if (slot < 0 || slot >= 54) return;

            ItemStack cursorItem = event.getCursor(); 
            ItemStack clickedItem = event.getCurrentItem();

            // --- CLIQUE ESQUERDO: Adicionar ou Substituir ---
            if (event.getClick() == ClickType.LEFT) {
                event.setCancelled(true);

                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    if (cursorItem.getType().getMaxStackSize() <= 1) return;

                    int allowedSlots = getMaxSlots(player, typeCode);
                    if (slot >= allowedSlots) {
                        player.sendMessage("§cThis slot is locked!");
                        return;
                    }

                    // Substituir
                    if (clickedItem != null && clickedItem.getType() != Material.AIR && clickedItem.getType() != Material.RED_STAINED_GLASS_PANE) {
                        player.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));
                        saveItem(player, typeCode, slot, cursorItem.getType().name());
                    } 
                    // Adicionar em slot vazio
                    else {
                        saveItem(player, typeCode, slot, cursorItem.getType().name());
                    }
                    
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
                    // Atualiza o menu sem fechar
                    FilterMenu.open(player, typeCode);
                    return;
                }
                return;
            }

            // --- 3. Botão Direito: Retirar/Deletar ---
            if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
                if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) return;

                if (isISF && event.getClick() == ClickType.RIGHT) {
                    String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeCode, slot);
                    if (matName != null) {
                        VirtualFilter.getInstance().getInfinityManager().withdrawPack(player, matName, slot);
                        // Aqui mantemos o fechamento para evitar conflito visual no ISF
                        player.closeInventory();
                    }
                    return;
                }

                if (VirtualFilter.getInstance().getDbManager().isConfirmEnabled(player.getUniqueId())) {
                    VirtualFilter.getInstance().getConfirmationManager().setPending(player.getUniqueId(), typeCode + ":" + slot);
                    player.closeInventory();
                    player.sendMessage("§7Type §ay §7to confirm deletion.");
                } else {
                    executeDelete(player, typeCode, slot);
                    // Na deleção direta, atualizamos sem fechar
                    FilterMenu.open(player, typeCode);
                }
            }
        }
    }

    private void handleQuickAdd(Player player, String type, Material mat) {
        int allowedSlots = getMaxSlots(player, type);
        int targetSlot = -1;
        for (int i = 0; i < allowedSlots; i++) {
            if (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i) == null) {
                targetSlot = i;
                break;
            }
        }
        if (targetSlot != -1) {
            saveItem(player, type, targetSlot, mat.name());
        }
    }

    private int getMaxSlots(Player player, String type) {
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int value = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (value > max) max = value;
                } catch (NumberFormatException ignored) {}
            }
        }
        return Math.min(max, 54);
    }

    private void saveItem(Player p, String type, int slot, String mat) {
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, 0)")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void executeDelete(Player player, String type, int slot) {
        String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, slot);
        if (matName != null) {
            Material m = Material.getMaterial(matName);
            if (m != null) player.getInventory().addItem(new ItemStack(m, 1));
        }
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.executeUpdate();
            player.sendMessage("§aFilter removed.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
