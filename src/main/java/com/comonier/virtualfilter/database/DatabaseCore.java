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
                s.execute("PRAGMA journal_mode = WAL;");
                s.execute("PRAGMA synchronous = NORMAL;");
                
                // Tabela original de filtros (Vanilla)
                s.execute("CREATE TABLE IF NOT EXISTS player_filters (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                
                // NOVA TABELA: player_filters_edit (Para itens com NBT/Nome Customizado)
                // Aqui o 'material' será usado como fallback, mas a chave será o 'display_name' ou 'nbt_hash'
                s.execute("CREATE TABLE IF NOT EXISTS player_filters_edit (uuid TEXT, filter_type TEXT, slot_id INTEGER, material TEXT, custom_name TEXT, amount BIGINT DEFAULT 0, PRIMARY KEY (uuid, filter_type, slot_id))");
                
                // Tabela de configurações
                s.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid TEXT PRIMARY KEY, actionbar_enabled BOOLEAN DEFAULT 1, language TEXT DEFAULT 'en', autofill_enabled BOOLEAN DEFAULT 1, autoloot_enabled BOOLEAN DEFAULT 0, logs_own BOOLEAN DEFAULT 1, logs_all BOOLEAN DEFAULT 1, safedrop_enabled BOOLEAN DEFAULT 1)");
                
                // Migrações de colunas
                try { s.execute("ALTER TABLE player_settings ADD COLUMN logs_own BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN logs_all BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                try { s.execute("ALTER TABLE player_settings ADD COLUMN safedrop_enabled BOOLEAN DEFAULT 1"); } catch (SQLException ignored) {}
                
                s.execute("DELETE FROM player_filters WHERE amount <= 0 AND filter_type = 'isf'");
                s.execute("DELETE FROM player_filters_edit WHERE amount <= 0");
                s.execute("REINDEX player_filters;");
                s.execute("REINDEX player_filters_edit;");
                s.execute("VACUUM;");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public Connection getConnection() { return connection; }
    public void closeConnection() {
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
