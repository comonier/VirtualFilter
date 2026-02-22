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

import java.util.HashMap;

public class AutoFillListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) return;

        ItemStack itemInHand = event.getItemInHand();
        if (itemInHand.getAmount() <= 1) {
            refill(player, itemInHand);
        }
    }

    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if (!VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) return;
        refill(player, event.getBrokenItem());
    }

    private void refill(Player player, ItemStack targetItem) {
        if (targetItem == null || targetItem.getType() == Material.AIR) return;
        Material type = targetItem.getType();
        
        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            if (currentHand != null && currentHand.getType() != Material.AIR) return;

            // 1. Prioridade: Inventário Físico
            for (int i = 0; i < 36; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (invItem != null && invItem.getType() == type) {
                    // Mantém a regra original de não repor itens com encantamentos para blocos
                    if (invItem.getEnchantments().isEmpty()) {
                        player.getInventory().setItemInMainHand(invItem);
                        player.getInventory().setItem(i, null);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                        return;
                    }
                }
            }

            // 2. Fallback: ISF (Estoque Virtual)
            if (type.getMaxStackSize() > 1) {
                int amountTaken = VirtualFilter.getInstance().getDbManager().withdrawFromISF(player.getUniqueId(), type.name(), 64);
                if (amountTaken > 0) {
                    ItemStack isfStack = new ItemStack(type, amountTaken);
                    player.getInventory().setItemInMainHand(isfStack);
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.4f, 1.2f);
                    
                    String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());
                    String msg = (lang.equalsIgnoreCase("pt")) ? 
                        "§6[VirtualFilter] §f" + type.name() + " §7reposto do estoque ISF!" : 
                        "§6[VirtualFilter] §f" + type.name() + " §7refilled from ISF storage!";
                    player.sendMessage(msg);
                }
            }
        });
    }
}
