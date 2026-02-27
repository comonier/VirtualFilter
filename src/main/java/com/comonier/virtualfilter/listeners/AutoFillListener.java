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
        if (false == VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) {
            return;
        }
        
        ItemStack itemInHand = event.getItemInHand();
        // Lógica inversa: Se 2 for maior que a quantidade (resta 1 ou 0)
        if (null != itemInHand && 2 > itemInHand.getAmount()) {
            refill(player, itemInHand);
        }
    }

    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if (false == VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId())) {
            return;
        }
        refill(player, event.getBrokenItem());
    }

    private void refill(Player player, ItemStack targetItem) {
        if (null == targetItem || targetItem.getType() == Material.AIR) {
            return;
        }
        Material type = targetItem.getType();

        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            // Se a mão não estiver vazia agora, não precisamos repor nada
            if (null != currentHand && currentHand.getType() != Material.AIR) {
                return;
            }

            // 1. TENTA REPOR DO INVENTÁRIO (Economiza o banco de dados)
            for (int i = 0; 36 > i; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                // Verifica se o item é igual, se não é nulo e se não tem encantamentos (segurança)
                if (null != invItem && invItem.getType() == type && invItem.getEnchantments().isEmpty()) {
                    player.getInventory().setItemInMainHand(invItem);
                    player.getInventory().setItem(i, null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                    return;
                }
            }

            // 2. SE NÃO ACHOU NO INV, TENTA REPOR DO ISF (Estoque Infinito)
            if (type.getMaxStackSize() > 1) {
                int amountTaken = (int) VirtualFilter.getInstance().getDbManager().withdrawFromISF(player.getUniqueId(), type.name(), 64);
                if (amountTaken > 0) {
                    player.getInventory().setItemInMainHand(new ItemStack(type, amountTaken));
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.4f, 1.2f);
                    
                    String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());
                    if (null == lang) {
                        lang = "en";
                    }
                    
                    String msg = VirtualFilter.getInstance().getMsg(lang, "isf_refill").replace("%item%", type.name());
                    player.sendMessage(msg);
                }
            }
        });
    }
}
