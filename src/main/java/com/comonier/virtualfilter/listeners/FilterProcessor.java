package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class FilterProcessor implements Listener {

    // 1. Coleta Manual (Caso o item já estivesse no chão)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (processItem(player, item)) {
            event.getItem().remove();
            event.setCancelled(true);
        }
    }

    // 2. AutoLoot de Bloco (Apenas remove o drop físico do bloco quebrado)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
            // Permitimos que o bloco quebre, mas o ItemSpawnEvent cuidará do recolhimento
            event.setDropItems(false); 
            
            // Forçamos o drop via código para garantir que o ItemSpawnEvent seja disparado 
            // e capturado pelo nosso ímã, respeitando Fortuna/SilkTouch
            event.getBlock().getDrops(player.getInventory().getItemInMainHand(), player).forEach(drop -> {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            });
        }
    }

    // 3. O ÍMÃ UNIVERSAL (Captura TUDO: Drops normais, mcMMO, Slimefun, Explosões)
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        
        // Raio de 10 blocos (Aumente se quiser um ímã mais potente)
        for (Entity entity : itemEntity.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Player player) {
                if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
                    ItemStack stack = itemEntity.getItemStack();
                    event.setCancelled(true); // Remove do chão
                    deliverItem(player, stack, itemEntity.getLocation());
                    return;
                }
            }
        }
    }

    private void deliverItem(Player player, ItemStack item, org.bukkit.Location loc) {
        if (processItem(player, item)) return;

        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);

        if (!left.isEmpty()) {
            for (ItemStack remaining : left.values()) {
                player.getWorld().dropItemNaturally(loc, remaining);
            }
        }
    }

    public boolean processItem(Player player, ItemStack item) {
        // SEGURANÇA TOTAL: Itens com nome customizado (Slimefun/mcMMO) NUNCA são filtrados.
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return false; 
        }

        String materialName = item.getType().name();
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());

        // ASF - Venda
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
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.2f);
            return true;
        }

        // ISF - Estoque
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "isf", materialName)) {
            VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), materialName, item.getAmount());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 1.8f);
            return true;
        }

        // ABF - Bloqueio
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "abf", materialName)) {
            return true;
        }

        return false;
    }
}
