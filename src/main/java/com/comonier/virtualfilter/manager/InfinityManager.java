package com.comonier.virtualfilter.manager;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class InfinityManager {
    public long getAmount(UUID uuid, String materialName) {
        String query = "SELECT amount FROM player_filters WHERE uuid = ? AND material = ? AND filter_type = 'isf'";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, materialName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("amount");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void updateAmount(UUID uuid, String materialName, long diff) {
        String query = "UPDATE player_filters SET amount = amount + ? WHERE uuid = ? AND material = ? AND filter_type = 'isf'";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setLong(1, diff);
            ps.setString(2, uuid.toString());
            ps.setString(3, materialName);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void withdrawPack(Player player, String materialName, int slot) {
        Material mat = Material.getMaterial(materialName);
        if (mat == null) return;
        long currentTotal = getAmount(player.getUniqueId(), materialName);
        if (currentTotal <= 0) return;
        int toWithdraw = (int) Math.min(currentTotal, mat.getMaxStackSize());
        ItemStack item = new ItemStack(mat, toWithdraw);
        if (player.getInventory().addItem(item).isEmpty()) {
            updateAmount(player.getUniqueId(), materialName, -toWithdraw);
        }
    }
}
