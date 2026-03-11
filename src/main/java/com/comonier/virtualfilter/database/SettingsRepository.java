package com.comonier.virtualfilter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SettingsRepository {
    private final Connection connection;

    public SettingsRepository(Connection connection) {
        this.connection = connection;
    }

    public String getPlayerLanguage(UUID uuid) {
        String query = "SELECT language FROM player_settings WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("language");
        } catch (SQLException e) { e.printStackTrace(); }
        return "en";
    }

    public boolean isAutoLootEnabled(UUID uuid) { return getBooleanSetting(uuid, "autoloot_enabled", false); }
    public boolean isAutoFillEnabled(UUID uuid) { return getBooleanSetting(uuid, "autofill_enabled", true); }
    public boolean isActionBarEnabled(UUID uuid) { return getBooleanSetting(uuid, "actionbar_enabled", true); }
    
    public boolean isLogsOwnEnabled(UUID uuid) { return getBooleanSetting(uuid, "logs_own", true); }
    public boolean isLogsAllEnabled(UUID uuid) { return getBooleanSetting(uuid, "logs_all", true); }
    
    public boolean isSafeDropEnabled(UUID uuid) { return getBooleanSetting(uuid, "safedrop_enabled", true); }

    private boolean getBooleanSetting(UUID uuid, String column, boolean defaultValue) {
        String query = "SELECT " + column + " FROM player_settings WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(column);
        } catch (SQLException e) { e.printStackTrace(); }
        return defaultValue;
    }

    public void updateSetting(UUID uuid, String column, Object value) {
        String query = "INSERT INTO player_settings (uuid, " + column + ") VALUES (?, ?) " +
                       "ON CONFLICT(uuid) DO UPDATE SET " + column + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setObject(2, value);
            ps.setObject(3, value);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleAutoLoot(UUID uuid) { updateSetting(uuid, "autoloot_enabled", !isAutoLootEnabled(uuid)); }
    public void toggleAutoFill(UUID uuid) { updateSetting(uuid, "autofill_enabled", !isAutoFillEnabled(uuid)); }
    public void toggleActionBar(UUID uuid) { updateSetting(uuid, "actionbar_enabled", !isActionBarEnabled(uuid)); }
    
    public void toggleLogsOwn(UUID uuid) { updateSetting(uuid, "logs_own", !isLogsOwnEnabled(uuid)); }
    public void toggleLogsAll(UUID uuid) { updateSetting(uuid, "logs_all", !isLogsAllEnabled(uuid)); }
    
    public void toggleSafeDrop(UUID uuid) { updateSetting(uuid, "safedrop_enabled", !isSafeDropEnabled(uuid)); }
}
