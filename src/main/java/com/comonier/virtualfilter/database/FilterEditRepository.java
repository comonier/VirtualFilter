package com.comonier.virtualfilter.database;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.sql.*;
import java.util.UUID;

public class FilterEditRepository {
    private final Connection connection;

    public FilterEditRepository(Connection connection) { this.connection = connection; }

    public synchronized boolean removeAndShift(UUID uuid, String type, int slotId) {
        String t = type.toLowerCase(); String u = uuid.toString();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_filters_edit WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
                ps.setString(1, u); ps.setString(2, t); ps.setInt(3, slotId); ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters_edit SET slot_id = (slot_id * -1) WHERE uuid = ? AND filter_type = ? AND slot_id > ?")) {
                ps.setString(1, u); ps.setString(2, t); ps.setInt(3, slotId); ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters_edit SET slot_id = (ABS(slot_id) - 1) WHERE uuid = ? AND filter_type = ? AND slot_id < 0")) {
                ps.setString(1, u); ps.setString(2, t); ps.executeUpdate();
            }
            connection.commit(); connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ignored) {}
            e.printStackTrace(); return false;
        }
    }

    public String[] getItemDataAtSlot(UUID uuid, String type, int slot) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT material, custom_name FROM player_filters_edit WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase()); ps.setInt(3, slot);
            ResultSet rs = ps.executeQuery(); 
            if (rs.next()) return new String[]{rs.getString("material"), rs.getString("custom_name")};
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public ItemStack getItemTemplate(UUID uuid, String material, String customName) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT item_data FROM player_filters_edit WHERE uuid = ? AND material = ? AND custom_name = ?")) {
            ps.setString(1, uuid.toString()); ps.setString(2, material.toUpperCase()); ps.setString(3, customName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] bytes = rs.getBytes("item_data");
                if (null != bytes) return ItemStack.deserializeBytes(bytes);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean hasFilter(UUID uuid, String type, String material, String customName) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM player_filters_edit WHERE uuid = ? AND filter_type = ? AND material = ? AND custom_name = ?")) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase()); 
            ps.setString(3, material.toUpperCase()); ps.setString(4, customName);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public long getISFEAmount(UUID uuid, String material, String customName) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT amount FROM player_filters_edit WHERE uuid = ? AND material = ? AND custom_name = ? AND filter_type = 'isfe'")) {
            ps.setString(1, uuid.toString()); ps.setString(2, material.toUpperCase()); ps.setString(3, customName);
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getLong("amount");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void addAmount(UUID uuid, String material, String customName, long amount) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters_edit SET amount = amount + ? WHERE uuid = ? AND material = ? AND custom_name = ? AND filter_type = 'isfe'")) {
            ps.setLong(1, amount); ps.setString(2, uuid.toString()); 
            ps.setString(3, material.toUpperCase()); ps.setString(4, customName); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int withdrawFromISFE(UUID uuid, String material, String customName, int requested) {
        long current = getISFEAmount(uuid, material, customName); if (current <= 0) return 0;
        int actual = (int) Math.min(current, (long) requested); addAmount(uuid, material, customName, -actual); return actual;
    }

    public void withdrawMassive(Player player, String materialName, String customName) {
        Material mat = Material.getMaterial(materialName); if (mat == null) return;
        UUID u = player.getUniqueId(); long currentTotal = getISFEAmount(u, materialName, customName);
        if (currentTotal <= 0) return; int maxStack = mat.getMaxStackSize(); long initialTotal = currentTotal;
        
        ItemStack template = getItemTemplate(u, materialName, customName);

        for (int i = 0; 36 > i; i++) {
            if (0 >= currentTotal) break; 
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                int taking = (int) Math.min(currentTotal, (long) maxStack);
                ItemStack newItem = (null != template) ? template.clone() : new ItemStack(mat);
                if (null == template) {
                    ItemMeta meta = newItem.getItemMeta();
                    if (null != meta) { meta.setDisplayName(customName); newItem.setItemMeta(meta); }
                }
                newItem.setAmount(taking);
                player.getInventory().setItem(i, newItem); currentTotal -= taking;
            }
        }
        long withdrawn = initialTotal - currentTotal;
        if (withdrawn > 0) addAmount(u, materialName, customName, -withdrawn);
    }
}
