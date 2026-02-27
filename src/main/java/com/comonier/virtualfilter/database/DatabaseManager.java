package com.comonier.virtualfilter.database;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;

    public void setupDatabase() {
        try {
            File folder = VirtualFilter.getInstance().getDataFolder();
            if (false == folder.exists()) {
                folder.mkdirs();
            }
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "storage.db"));
            
            try (Statement s = connection.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS player_filters (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                s.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid TEXT PRIMARY KEY, actionbar_enabled BOOLEAN DEFAULT 1, language TEXT DEFAULT 'en', autofill_enabled BOOLEAN DEFAULT 1, autoloot_enabled BOOLEAN DEFAULT 0, chest_debug BOOLEAN DEFAULT 1)");
                
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autofill_enabled BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autoloot_enabled BOOLEAN DEFAULT 0"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN chest_debug BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    public Connection getConnection() { 
        return connection; 
    }

    public boolean removeAndShift(UUID uuid, String type, int slotId) {
        try {
            String typeLower = type.toLowerCase();
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, typeLower);
                ps.setInt(3, slotId);
                if (0 >= ps.executeUpdate()) {
                    return false;
                }
            }
            try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET slot_id = slot_id - 1 WHERE uuid = ? AND filter_type = ? AND slot_id > ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, typeLower);
                ps.setInt(3, slotId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    public boolean removeByMaterial(UUID uuid, String type, String material) {
        try {
            int slot = -1;
            try (PreparedStatement ps = connection.prepareStatement("SELECT slot_id FROM player_filters WHERE uuid = ? AND filter_type = ? AND material = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, type.toLowerCase());
                ps.setString(3, material.toUpperCase());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    slot = rs.getInt("slot_id");
                }
            }
            if (-1 != slot) {
                return removeAndShift(uuid, type, slot);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public String getMaterialAtSlot(UUID uuid, String type, int slot) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT material FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, uuid.toString());
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

    public long getISFAmount(UUID uuid, String material) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT amount FROM player_filters WHERE uuid = ? AND material = ? AND filter_type = 'isf'")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, material.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("amount");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0;
    }

    public void addAmount(UUID uuid, String material, long amount) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET amount = amount + ? WHERE uuid = ? AND material = ? AND filter_type = 'isf'")) {
            ps.setLong(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, material.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    public int withdrawFromISF(UUID uuid, String material, int requested) {
        long current = getISFAmount(uuid, material);
        if (0 >= current) {
            return 0;
        }
        int actual = (int) Math.min(current, (long) requested);
        addAmount(uuid, material, -actual);
        return actual;
    }

    public boolean hasFilter(UUID uuid, String type, String material) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM player_filters WHERE uuid = ? AND filter_type = ? AND material = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.toLowerCase());
            ps.setString(3, material.toUpperCase());
            return ps.executeQuery().next();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public String getPlayerLanguage(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT language FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("language");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return "en";
    }

    public boolean isAutoLootEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT autoloot_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("autoloot_enabled");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public boolean isChestDebugEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT chest_debug FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("chest_debug");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return true;
    }

    public boolean isAutoFillEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT autofill_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("autofill_enabled");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return true;
    }

    public void updateSetting(UUID uuid, String column, Object value) {
        String query = "INSERT INTO player_settings (uuid, " + column + ") VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET " + column + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setObject(2, value);
            ps.setObject(3, value);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    // MÉTODOS EXIGIDOS PELOS COMANDOS (RESTAURADOS)
    public void toggleAutoLoot(UUID uuid) { updateSetting(uuid, "autoloot_enabled", !isAutoLootEnabled(uuid)); }
    public void toggleChestDebug(UUID uuid) { updateSetting(uuid, "chest_debug", !isChestDebugEnabled(uuid)); }
    public void toggleAutoFill(UUID uuid) { updateSetting(uuid, "autofill_enabled", !isAutoFillEnabled(uuid)); }
}
