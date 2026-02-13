package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class FilterProcessor implements Listener {

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        String materialName = item.getType().name();

        // 1. ABF - Bloqueio
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "abf", materialName)) {
            event.getItem().remove();
            event.setCancelled(true);
            return;
        }

        // 2. ASF - Venda Automática (Auto Sell)
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "asf", materialName)) {
            double unitPrice = ShopGUIHook.getItemPrice(player, item);
            if (unitPrice > 0) {
                double totalValue = unitPrice * item.getAmount();
                
                if (VirtualFilter.getEconomy() != null) {
                    VirtualFilter.getEconomy().depositPlayer(player, totalValue);
                    
                    // Envia notificação na Action Bar se estiver ativado pelo player
                    if (VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId())) {
                        String message = String.format("§a+$%.2f §7(Sold %dx %s)", totalValue, item.getAmount(), materialName);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                    }
                }
                
                event.getItem().remove();
                event.setCancelled(true);
                return;
            }
        }

        // 3. ISF - Estoque Infinito
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "isf", materialName)) {
            VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), materialName, item.getAmount());
            event.getItem().remove();
            event.setCancelled(true);
        }
    }
}
