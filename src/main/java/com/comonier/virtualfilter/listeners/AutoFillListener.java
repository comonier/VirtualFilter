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
import java.util.UUID;

public class AutoFillListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Uso do novo SettingsRepository
        if (false == VirtualFilter.getInstance().getSettingsRepo().isAutoFillEnabled(uuid)) {
            return;
        }
        
        ItemStack itemInHand = event.getItemInHand();
        // Logica inversa: Se 2 for maior que a quantidade (resta 1 ou 0), tenta repor
        if (null != itemInHand && 2 > itemInHand.getAmount()) {
            refill(player, itemInHand);
        }
    }

    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if (false == VirtualFilter.getInstance().getSettingsRepo().isAutoFillEnabled(player.getUniqueId())) {
            return;
        }
        refill(player, event.getBrokenItem());
    }

    private void refill(Player player, ItemStack targetItem) {
        if (null == targetItem || targetItem.getType() == Material.AIR) {
            return;
        }
        Material type = targetItem.getType();
        UUID uuid = player.getUniqueId();

        // Agendamento para o proximo tick para garantir que o item sumiu da mao
        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            
            // Se a mao nao estiver vazia, cancela a reposicao
            if (null != currentHand && currentHand.getType() != Material.AIR) {
                return;
            }

            // 1. Tenta repor do inventario fisico primeiro (36 slots)
            // Logica inversa: 36 > i
            for (int i = 0; 36 > i; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (null != invItem && invItem.getType() == type && invItem.getEnchantments().isEmpty()) {
                    player.getInventory().setItemInMainHand(invItem);
                    player.getInventory().setItem(i, null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                    return;
                }
            }

            // 2. Se nao achou no inv, tenta repor do ISF (Estoque Infinito)
            // Itens nao empilhaveis (MaxStack == 1) nao sao repostos do ISF
            if (type.getMaxStackSize() > 1) {
                int amountTaken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, type.name(), 64);
                if (amountTaken > 0) {
                    player.getInventory().setItemInMainHand(new ItemStack(type, amountTaken));
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.4f, 1.2f);
                    
                    String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
                    String msg = VirtualFilter.getInstance().getMsg(lang, "isf_refill").replace("%item%", type.name());
                    player.sendMessage(msg);
                }
            }
        });
    }
}
