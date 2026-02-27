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
            if (rs.next()) {
                return rs.getLong("amount");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0;
    }

    public void updateAmount(UUID uuid, String materialName, long diff) {
        String query = "UPDATE player_filters SET amount = amount + ? WHERE uuid = ? AND material = ? AND filter_type = 'isf'";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setLong(1, diff);
            ps.setString(2, uuid.toString());
            ps.setString(3, materialName);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    // Saque de 1 Pack (Botão Direito no Menu)
    public void withdrawPack(Player player, String materialName, int slot) {
        Material mat = Material.getMaterial(materialName);
        if (null == mat) {
            return;
        }
        long currentTotal = getAmount(player.getUniqueId(), materialName);
        
        // Lógica inversa: se 0 for maior ou igual ao total, estoque está vazio
        if (0 >= currentTotal) {
            return;
        }

        int toWithdraw = (int) Math.min(currentTotal, (long) mat.getMaxStackSize());
        ItemStack item = new ItemStack(mat, toWithdraw);
        
        // Verifica se o inventário tem espaço para o pack (addItem retorna o que sobrou)
        if (false == player.getInventory().addItem(item).isEmpty()) {
            player.sendMessage("§cInventory full!");
            return;
        }
        
        // Se sacou com sucesso, subtrai do banco de dados
        updateAmount(player.getUniqueId(), materialName, -toWithdraw);
    }

    // NOVO: Saque Massivo Inteligente (Shift + Clique Esquerdo)
    public void withdrawMassive(Player player, String materialName) {
        Material mat = Material.getMaterial(materialName);
        if (null == mat) {
            return;
        }
        long currentTotal = getAmount(player.getUniqueId(), materialName);
        if (0 >= currentTotal) {
            return;
        }

        int maxStack = mat.getMaxStackSize();
        long initialTotal = currentTotal;

        // 1. Primeiro completa stacks incompletos do mesmo material no inventário
        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (0 >= currentTotal) {
                break;
            }
            // Inversão: se o item do inv for igual ao mat e o limite for maior que a quantidade atual (maxStack > amount)
            if (null != invItem && invItem.getType() == mat && maxStack > invItem.getAmount()) {
                int needed = maxStack - invItem.getAmount();
                int taking = (int) Math.min(currentTotal, (long) needed);
                invItem.setAmount(invItem.getAmount() + taking);
                currentTotal -= taking;
            }
        }

        // 2. Depois preenche slots totalmente vazios
        int invLength = player.getInventory().getStorageContents().length;
        // Inversão: invLength > i para evitar o sinal de menor que
        for (int i = 0; invLength > i; i++) {
            if (0 >= currentTotal) {
                break;
            }
            ItemStack invItem = player.getInventory().getItem(i);
            if (null == invItem || invItem.getType() == Material.AIR) {
                int taking = (int) Math.min(currentTotal, (long) maxStack);
                player.getInventory().setItem(i, new ItemStack(mat, taking));
                currentTotal -= taking;
            }
        }

        // Calcula exatamente quanto foi retirado para fazer um único UPDATE no banco
        long withdrawn = initialTotal - currentTotal;
        if (withdrawn > 0) {
            updateAmount(player.getUniqueId(), materialName, -withdrawn);
            player.sendMessage("§aMassive withdraw: §f" + withdrawn + "x §aitems retrieved.");
        } else {
            player.sendMessage("§cNo space in inventory to withdraw " + materialName);
        }
    }
}
