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

    // Saque de 1 Pack (Botão Direito)
    public void withdrawPack(Player player, String materialName, int slot) {
        Material mat = Material.getMaterial(materialName);
        if (mat == null) return;
        long currentTotal = getAmount(player.getUniqueId(), materialName);
        if (currentTotal <= 0) return;

        int toWithdraw = (int) Math.min(currentTotal, mat.getMaxStackSize());
        ItemStack item = new ItemStack(mat, toWithdraw);
        if (!player.getInventory().addItem(item).isEmpty()) {
            player.sendMessage("§cInventory full!");
            return;
        }
        updateAmount(player.getUniqueId(), materialName, -toWithdraw);
    }

    // NOVO: Saque Massivo Inteligente (Shift + Clique Esquerdo)
    public void withdrawMassive(Player player, String materialName) {
        Material mat = Material.getMaterial(materialName);
        if (mat == null) return;
        long currentTotal = getAmount(player.getUniqueId(), materialName);
        if (currentTotal <= 0) return;

        int maxStack = mat.getMaxStackSize();
        long initialTotal = currentTotal;

        // 1. Primeiro completa stacks incompletos no inventário
        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (currentTotal <= 0) break;
            if (invItem != null && invItem.getType() == mat && invItem.getAmount() < maxStack) {
                int needed = maxStack - invItem.getAmount();
                int taking = (int) Math.min(currentTotal, needed);
                invItem.setAmount(invItem.getAmount() + taking);
                currentTotal -= taking;
            }
        }

        // 2. Depois preenche slots vazios
        for (int i = 0; i < player.getInventory().getStorageContents().length; i++) {
            if (currentTotal <= 0) break;
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem == null || invItem.getType() == Material.AIR) {
                int taking = (int) Math.min(currentTotal, maxStack);
                player.getInventory().setItem(i, new ItemStack(mat, taking));
                currentTotal -= taking;
            }
        }

        long withdrawn = initialTotal - currentTotal;
        if (withdrawn > 0) {
            updateAmount(player.getUniqueId(), materialName, -withdrawn);
            player.sendMessage("§aMassive withdraw: §f" + withdrawn + "x §aitems retrieved.");
        } else {
            player.sendMessage("§cNo space in inventory to withdraw " + materialName);
        }
    }
}
