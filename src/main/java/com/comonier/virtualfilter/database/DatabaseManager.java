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
                // Tabela de filtros
                s.execute("CREATE TABLE IF NOT EXISTS player_filters (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                // Tabela de configurações pessoais (Toggle Action Bar, Confirmação e Idioma)
                s.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid TEXT PRIMARY KEY, actionbar_enabled BOOLEAN DEFAULT 1, confirm_enabled BOOLEAN DEFAULT 1, language TEXT DEFAULT 'en')");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Connection getConnection() { return connection; }

    // --- Configurações de Idioma ---
    public String getPlayerLanguage(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT language FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String lang = rs.getString("language");
                if (lang != null) return lang;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return VirtualFilter.getInstance().getConfig().getString("language", "en");
    }

    public void setPlayerLanguage(UUID uuid, String lang) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_settings (uuid, language) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET language = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, lang);
            ps.setString(3, lang);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Configurações de Action Bar ---
    public boolean isActionBarEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT actionbar_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("actionbar_enabled");
        } catch (SQLException e) { e.printStackTrace(); }
        return VirtualFilter.getInstance().getConfig().getBoolean("default-actionbar-enabled", true);
    }

    public void toggleActionBar(UUID uuid) {
        boolean current = isActionBarEnabled(uuid);
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_settings (uuid, actionbar_enabled) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET actionbar_enabled = ?")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, !current);
            ps.setBoolean(3, !current);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Configurações de Confirmação ---
    public boolean isConfirmEnabled(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT confirm_enabled FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("confirm_enabled");
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    public void toggleConfirmation(UUID uuid) {
        boolean current = isConfirmEnabled(uuid);
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_settings (uuid, confirm_enabled) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET confirm_enabled = ?")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, !current);
            ps.setBoolean(3, !current);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Métodos de Filtros ---
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
