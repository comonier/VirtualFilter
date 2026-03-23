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
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

public class AutoFillListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (false == VirtualFilter.getInstance().getSettingsRepo().isAutoFillEnabled(uuid)) {
            return;
        }
        
        ItemStack itemInHand = event.getItemInHand();
        // Se o item na mao acabou ou tem apenas 1 (sera o ultimo bloco colocado)
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
        
        // Verifica se o item que acabou eh um item editado (ISFE)
        boolean isEdit = (targetItem.hasItemMeta() && targetItem.getItemMeta().hasDisplayName());
        String customName = isEdit ? targetItem.getItemMeta().getDisplayName() : null;

        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            
            // Se a mao ja foi preenchida por algum motivo, cancela
            if (null != currentHand && currentHand.getType() != Material.AIR) {
                return;
            }

            // 1. Busca primeiro no INVENTARIO fisico do jogador
            for (int i = 0; 36 > i; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (null != invItem && invItem.getType() == type) {
                    
                    if (isEdit) {
                        // Se o item original era editado, so repoe se o do inv for identico (mesmo nome)
                        if (invItem.hasItemMeta() && customName.equals(invItem.getItemMeta().getDisplayName())) {
                            player.getInventory().setItemInMainHand(invItem);
                            player.getInventory().setItem(i, null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                            return;
                        }
                    } else {
                        // Se o item era COMUM, so repoe se o do inv TAMBEM for comum (sem meta/nome)
                        if (false == invItem.hasItemMeta() || (false == invItem.getItemMeta().hasDisplayName() && false == invItem.getItemMeta().hasCustomModelData())) {
                            if (invItem.getEnchantments().isEmpty()) {
                                player.getInventory().setItemInMainHand(invItem);
                                player.getInventory().setItem(i, null);
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                                return;
                            }
                        }
                    }
                }
            }

            // 2. Se nao achou no inv, busca nos ESTOQUES VIRTUAIS (ISF/ISFE)
            if (type.getMaxStackSize() > 1) {
                int amountTaken = 0;
                ItemStack template = null;
                
                if (isEdit) {
                    // Puxa do ISFE (Editados) respeitando material e nome
                    amountTaken = VirtualFilter.getInstance().getFilterEditRepo().withdrawFromISFE(uuid, type.name(), customName, 64);
                    template = VirtualFilter.getInstance().getFilterEditRepo().getItemTemplate(uuid, type.name(), customName);
                } else {
                    // Puxa do ISF (Comum)
                    amountTaken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, type.name(), 64);
                }

                if (amountTaken > 0) {
                    ItemStack newItem;
                    if (isEdit && null != template) {
                        newItem = template.clone();
                        newItem.setAmount(amountTaken);
                    } else {
                        newItem = new ItemStack(type, amountTaken);
                        // Se for editado mas sem template salvo (fallback), aplica o nome
                        if (isEdit) {
                            ItemMeta meta = newItem.getItemMeta();
                            if (null != meta) {
                                meta.setDisplayName(customName);
                                newItem.setItemMeta(meta);
                            }
                        }
                    }
                    
                    player.getInventory().setItemInMainHand(newItem);
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.4f, 1.2f);
                    
                    String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
                    String displayName = isEdit ? customName : type.name();
                    String msg = VirtualFilter.getInstance().getMsg(lang, "isf_refill").replace("%item%", displayName);
                    player.sendMessage(msg);
                }
            }
        });
    }
}
