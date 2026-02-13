package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!VirtualFilter.getInstance().getConfirmationManager().hasPending(player.getUniqueId())) return;
        
        String pending = VirtualFilter.getInstance().getConfirmationManager().getPending(player.getUniqueId());
        event.setCancelled(true);
        String msg = event.getMessage().toLowerCase();

        if (msg.equals("y")) {
            int sep = pending.indexOf(":");
            if (sep != -1) {
                String type = pending.substring(0, sep);
                int slot = Integer.parseInt(pending.substring(sep + 1));
                executeDelete(player, type, slot);
            }
        }
        VirtualFilter.getInstance().getConfirmationManager().clearPending(player.getUniqueId());
    }

    private void executeDelete(Player player, String type, int slot) {
        String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, slot);
        if (matName != null) {
            Material m = Material.getMaterial(matName);
            if (m != null) player.getInventory().addItem(new ItemStack(m, 1));
        }
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "DELETE FROM player_filters WHERE uuid = ? AND filter_type = ? AND slot_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.executeUpdate();
            player.sendMessage("§aFilter removed.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
