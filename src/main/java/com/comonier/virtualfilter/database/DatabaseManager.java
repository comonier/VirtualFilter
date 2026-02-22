package com.comonier.virtualfilter.database;

import com.comonier.virtualfilter.VirtualFilter;
import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;

    public void setupDatabase() {
        try {
            File folder = VirtualFilter.getInstance().getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "storage.db"));
            try (Statement s = connection.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS player_filters (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                s.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid TEXT PRIMARY KEY, actionbar_enabled BOOLEAN DEFAULT 1, language TEXT DEFAULT 'en', autofill_enabled BOOLEAN DEFAULT 1, autoloot_enabled BOOLEAN DEFAULT 0)");

                // MIGRAÇÃO: Adiciona colunas se não existirem
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autofill_enabled BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autoloot_enabled BOOLEAN DEFAULT 0"); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Connection getConnection() { return connection; }

    public String getPlayerLanguage(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT language FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("language");
        } catch (SQLException e) { e.printStackTrace(); }
        return VirtualFilter.getInstance().getConfig().getString("language", "en");
    }

    public void setPlayerLanguage(UUID uuid, String lang) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO player_settings (uuid, language, actionbar_enabled, autofill_enabled, autoloot_enabled) VALUES (?, ?, (SELECT actionbar_enabled FROM player_settings WHERE uuid = ?), (SELECT autofill_enabled FROM player_settings WHERE uuid = ?), (SELECT autoloot_enabled FROM player_settings WHERE uuid = ?))")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, lang);
            ps.setString(3, uuid.toString());
            ps.setString(4, uuid.toString());
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isActionBarEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT actionbar_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("actionbar_enabled");
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    public void toggleActionBar(UUID uuid) {
        boolean current = isActionBarEnabled(uuid);
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO player_settings (uuid, actionbar_enabled, language, autofill_enabled, autoloot_enabled) VALUES (?, ?, (SELECT language FROM player_settings WHERE uuid = ?), (SELECT autofill_enabled FROM player_settings WHERE uuid = ?), (SELECT autoloot_enabled FROM player_settings WHERE uuid = ?))")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, !current);
            ps.setString(3, uuid.toString());
            ps.setString(4, uuid.toString());
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isAutoFillEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT autofill_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("autofill_enabled");
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    public void toggleAutoFill(UUID uuid) {
        boolean current = isAutoFillEnabled(uuid);
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO player_settings (uuid, autofill_enabled, language, actionbar_enabled, autoloot_enabled) VALUES (?, ?, (SELECT language FROM player_settings WHERE uuid = ?), (SELECT actionbar_enabled FROM player_settings WHERE uuid = ?), (SELECT autoloot_enabled FROM player_settings WHERE uuid = ?))")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, !current);
            ps.setString(3, uuid.toString());
            ps.setString(4, uuid.toString());
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- NOVA FUNÇÃO: AutoLoot ---
    public boolean isAutoLootEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT autoloot_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("autoloot_enabled");
        } catch (SQLException e) { e.printStackTrace(); }
        return false; // Padrão desligado
    }

    public void toggleAutoLoot(UUID uuid) {
        boolean current = isAutoLootEnabled(uuid);
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO player_settings (uuid, autoloot_enabled, language, actionbar_enabled, autofill_enabled) VALUES (?, ?, (SELECT language FROM player_settings WHERE uuid = ?), (SELECT actionbar_enabled FROM player_settings WHERE uuid = ?), (SELECT autofill_enabled FROM player_settings WHERE uuid = ?))")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, !current);
            ps.setString(3, uuid.toString());
            ps.setString(4, uuid.toString());
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getMaterialAtSlot(UUID uuid, String type, int slot) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT material FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("material");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean hasFilter(UUID uuid, String type, String material) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM player_filters WHERE uuid = ? AND filter_type = ? AND material = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.toLowerCase());
            ps.setString(3, material);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void addAmount(UUID uuid, String material, int amount) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE player_filters SET amount = amount + ? WHERE uuid = ? AND material = ? AND filter_type = 'isf'")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, material);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
