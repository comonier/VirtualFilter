package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class AutoFillListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Verifica se a função está ativada para o jogador no DB
        if (!VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) return;

        ItemStack itemInHand = event.getItemInHand();
        // Se o jogador colocou o último bloco do pack (quantidade 1 antes de sumir)
        if (itemInHand.getAmount() <= 1) {
            refill(player, itemInHand);
        }
    }

    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        
        // Verifica se a função está ativada para o jogador no DB
        if (!VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) return;
        
        refill(player, event.getBrokenItem());
    }

    private void refill(Player player, ItemStack targetItem) {
        Material type = targetItem.getType();
        
        // Aguarda 1 tick para que o item atual suma da mão antes de repormos
        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            
            // Só repõe se a mão estiver realmente vazia agora
            if (currentHand != null && currentHand.getType() != Material.AIR) return;

            // Procura no inventário (slots 0 a 35)
            for (int i = 0; i < 36; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                
                if (invItem != null && invItem.getType() == type) {
                    // CONDIÇÃO: Deve ser idêntico em tipo e NÃO possuir encantamentos
                    if (invItem.getEnchantments().isEmpty()) {
                        player.getInventory().setItemInMainHand(invItem);
                        player.getInventory().setItem(i, null);
                        
                        // Som sutil de "pop" para o jogador notar a reposição
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 2.0f);
                        return;
                    }
                }
            }
        });
    }
}
