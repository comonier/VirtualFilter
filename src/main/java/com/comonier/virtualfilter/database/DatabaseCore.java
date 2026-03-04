package com.comonier.virtualfilter.database;
import com.comonier.virtualfilter.VirtualFilter;
import java.io.File;
import java.sql.*;
public class DatabaseCore {
    private Connection connection;
    public void setupDatabase() {
        try {
            File folder = VirtualFilter.getInstance().getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "storage.db"));
            try (Statement s = connection.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS player_filters (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                s.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid TEXT PRIMARY KEY, actionbar_enabled BOOLEAN DEFAULT 1, language TEXT DEFAULT 'en', autofill_enabled BOOLEAN DEFAULT 1, autoloot_enabled BOOLEAN DEFAULT 0, logs_own BOOLEAN DEFAULT 1, logs_all BOOLEAN DEFAULT 1)");
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autofill_enabled BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN autoloot_enabled BOOLEAN DEFAULT 0"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN logs_own BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN logs_all BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                s.execute("DELETE FROM player_filters WHERE filter_type = 'isf' AND (amount <= 0 OR amount IS NULL)");
                s.execute("CREATE INDEX IF NOT EXISTS idx_vfilter_lookup ON player_filters (uuid, filter_type, slot_id)");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public Connection getConnection() { return connection; }
    public void closeConnection() {
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
