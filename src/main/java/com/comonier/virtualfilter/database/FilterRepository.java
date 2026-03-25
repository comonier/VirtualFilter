package com.comonier.virtualfilter.database;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.*;
import java.util.UUID;

public class FilterRepository {
    private final Connection connection;

    public FilterRepository(Connection connection) { 
        this.connection = connection; 
    }

    public synchronized boolean removeAndShift(UUID uuid, String type, int slotId) {
        String t = type.toLowerCase(); 
        String u = uuid.toString();
        try {
            connection.setAutoCommit(false);
            
            // 1. Remove o filtro alvo
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
                ps.setString(1, u); ps.setString(2, t); ps.setInt(3, slotId); 
                ps.executeUpdate();
            }
            
            // 2. Move os slots afetados para uma zona temporaria negativa para evitar UNIQUE constraint
            try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET slot_id = (slot_id * -1) - 100 WHERE uuid = ? AND filter_type = ? AND slot_id > ?")) {
                ps.setString(1, u); ps.setString(2, t); ps.setInt(3, slotId); 
                ps.executeUpdate();
            }
            
            // 3. Traz de volta para o ID correto (ID original - 1)
            try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET slot_id = (ABS(slot_id + 100) - 1) WHERE uuid = ? AND filter_type = ? AND 0 > slot_id")) {
                ps.setString(1, u); ps.setString(2, t); 
                ps.executeUpdate();
            }
            
            connection.commit(); 
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ignored) {}
            e.printStackTrace(); 
            return false;
        }
    }

    public String getMaterialAtSlot(UUID uuid, String type, int slot) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT material FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase()); ps.setInt(3, slot);
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getString("material");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean hasFilter(UUID uuid, String type, String material) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM player_filters WHERE uuid = ? AND filter_type = ? AND material = ?")) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase()); ps.setString(3, material.toUpperCase());
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public long getISFAmount(UUID uuid, String material) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT amount FROM player_filters WHERE uuid = ? AND material = ? AND filter_type = 'isf'")) {
            ps.setString(1, uuid.toString()); ps.setString(2, material.toUpperCase());
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getLong("amount");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void addAmount(UUID uuid, String material, long amount) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET amount = amount + ? WHERE uuid = ? AND material = ? AND filter_type = 'isf'")) {
            ps.setLong(1, amount); ps.setString(2, uuid.toString()); ps.setString(3, material.toUpperCase()); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int withdrawFromISF(UUID uuid, String material, int requested) {
        long current = getISFAmount(uuid, material); 
        if (0 >= current) return 0;
        int actual = (int) Math.min(current, (long) requested); 
        addAmount(uuid, material, -actual); 
        return actual;
    }

    public void withdrawMassive(Player player, String materialName) {
        Material mat = Material.getMaterial(materialName); 
        if (mat == null) return;
        UUID u = player.getUniqueId(); 
        long currentTotal = getISFAmount(u, materialName);
        if (0 >= currentTotal) return; 
        
        int maxStack = mat.getMaxStackSize(); 
        long initialTotal = currentTotal;

        for (int i = 0; 36 > i; i++) {
            if (0 >= currentTotal) break; 
            ItemStack item = player.getInventory().getItem(i);
            
            if (item == null || item.getType() == Material.AIR) {
                int taking = (int) Math.min(currentTotal, (long) maxStack);
                player.getInventory().setItem(i, new ItemStack(mat, taking)); 
                currentTotal -= taking;
            } 
            else if (item.getType() == mat) {
                if (item.hasItemMeta()) {
                    if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasCustomModelData()) {
                        continue; 
                    }
                }
                if (maxStack > item.getAmount()) {
                    int taking = (int) Math.min(currentTotal, (long) (maxStack - item.getAmount()));
                    item.setAmount(item.getAmount() + taking); 
                    currentTotal -= taking;
                }
            }
        }
        long withdrawn = initialTotal - currentTotal;
        if (withdrawn > 0) addAmount(u, materialName, -withdrawn);
        
        if (currentTotal > 0) { 
            player.sendMessage("§6[VF] §c§lFULL INVENTORY!"); 
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f); 
        }
    }
}
