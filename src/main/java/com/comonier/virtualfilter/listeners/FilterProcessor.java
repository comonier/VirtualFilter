package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class FilterProcessor implements Listener {

    // --- EVENTO 1: Coleta do Chão (AutoLoot OFF ou itens dropados por outros) ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack item = event.getItem().getItemStack();
        if (processItem(player, item)) {
            event.getItem().remove();
            event.setCancelled(true);
        }
    }

    // --- EVENTO 2: Quebra de Bloco (AutoLoot ON) ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (!VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) return;

        // Pega os drops que o bloco geraria (respeita ferramenta, fortuna, etc)
        Collection<ItemStack> drops = event.getBlock().getDrops(player.getInventory().getItemInMainHand());
        
        // Cancela o drop físico no mundo
        event.setDropItems(false);

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType() == Material.AIR) continue;
            
            // Passa pelo funil de filtros
            boolean consumed = processItem(player, drop);
            
            // Se nenhum filtro (ASF/ISF/ABF) consumiu o item, ele vai para o inventário
            if (!consumed) {
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(drop);
                // Se o inventário lotar durante o AutoLoot, dropa o que sobrou no chão
                if (!left.isEmpty()) {
                    for (ItemStack remaining : left.values()) {
                        player.getWorld().dropItemNaturally(event.getBlock().getLocation(), remaining);
                    }
                }
            }
        }
    }

    /**
     * O FUNIL CENTRAL: Processa um item através de todos os sistemas de filtragem.
     * @return true se o item foi consumido (venda/estoque/bloqueio), false se deve ir ao inv.
     */
    public boolean processItem(Player player, ItemStack item) {
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
            return true; // Item consumido pela venda
        }

        // 2. ISF - ESTOQUE (Prioridade sobre ABF)
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "isf", materialName)) {
            VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), materialName, item.getAmount());
            if (VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId())) {
                String msg = VirtualFilter.getInstance().getMsg(lang, "isf_actionbar")
                        .replace("%amount%", String.valueOf(item.getAmount()))
                        .replace("%item%", materialName);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            }
            return true; // Item consumido pelo estoque
        }

        // 3. ABF - BLOQUEIO (Apenas impede a entrada)
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "abf", materialName)) {
            return true; // Item "consumido" pelo bloqueio (ignorado)
        }

        return false; // Item não filtrado, pode ir para o inventário
    }
}
