package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class FilterProcessor implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        String materialName = item.getType().name();
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());

        // 1. ASF - VENDA (Prioridade Total)
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "asf", materialName)) {
            double unitPrice = ShopGUIHook.getItemPrice(player, item);
            double totalValue = unitPrice * item.getAmount();
            
            if (VirtualFilter.getEconomy() != null) {
                VirtualFilter.getEconomy().depositPlayer(player, totalValue);
                if (VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId())) {
                    String msg = VirtualFilter.getInstance().getMsg(lang, "asf_actionbar")
                        .replace("%price%", String.format("%.2f", totalValue))
                        .replace("%amount%", String.valueOf(item.getAmount()))
                        .replace("%item%", materialName);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                }
            }
            event.getItem().remove();
            event.setCancelled(true);
            return;
        }

        // 2. ISF - ESTOQUE (Prioridade sobre ABF)
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "isf", materialName)) {
            VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), materialName, item.getAmount());
            event.getItem().remove();
            event.setCancelled(true);
            return;
        }

        // 3. ABF - APENAS IMPEDIR ENTRADA NO INVENTÁRIO
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "abf", materialName)) {
            // Cancelamos o evento de "pegar", o item continua no chão para outros ou some do fluxo de coleta do player
            event.setCancelled(true);
        }
    }
}
