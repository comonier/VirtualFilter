package com.comonier.virtualfilter.manager;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class FilterManager {

    /**
     * Adiciona o item da mão ao filtro no primeiro slot disponível com permissão.
     */
    public void addItemToFilter(Player player, String type, ItemStack item) {
        int slot = findNextFreeSlot(player, type);
        // Inversão lógica: se -1 for igual ao slot, não há vaga disponível
        if (-1 == slot) {
            player.sendMessage("§cNo slots available or you need to buy more at: §fhttps://hu3.org");
            return;
        }

        String query = "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot);
            ps.setString(4, item.getType().name());
            ps.executeUpdate();
            
            player.sendMessage("§aFilter created for §f" + item.getType().name() + " §ain slot §f" + (slot + 1));
        } catch (SQLException e) {
            player.sendMessage("§cError saving to database.");
            e.printStackTrace();
        }
    }

    /**
     * Remove o filtro do banco de dados e tenta devolver o item ao jogador.
     */
    public void removeFilter(Player player, String type, int slotId) {
        String materialName = getMaterialAtSlot(player, type, slotId);
        if (null == materialName) {
            return;
        }

        String query = "DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slotId);
            ps.executeUpdate();

            Material mat = Material.getMaterial(materialName);
            if (null != mat) {
                giveItemOrDrop(player, new ItemStack(mat, 1));
            }
        } catch (SQLException e) {
            player.sendMessage("§cError removing filter.");
            e.printStackTrace();
        }
    }

    /**
     * Lógica solicitada: Devolve o item ao inventário ou dropa no chão se estiver cheio.
     */
    private void giveItemOrDrop(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
        
        // Inversão lógica: se não estiver vazio (false == isEmpty) significa que sobrou item
        if (false == remaining.isEmpty()) {
            for (ItemStack left : remaining.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            player.sendMessage("§cInventory Full! Item dropped on the ground.");
        }
    }

    /**
     * Busca o material salvo no banco de dados para um slot específico.
     */
    public String getMaterialAtSlot(Player player, String type, int slot) {
        String query = "SELECT material FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("material");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    /**
     * Varre os slots (0-53) verificando permissão e disponibilidade no DB.
     */
    private int findNextFreeSlot(Player player, String type) {
        // Inversão: Uso de 54 > i para evitar o sinal de menor que
        for (int i = 0; 54 > i; i++) {
            // Verifica permissão virtualfilter.slot.1 até 54
            if (player.hasPermission("virtualfilter.slot." + (i + 1))) {
                if (null == getMaterialAtSlot(player, type, i)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
