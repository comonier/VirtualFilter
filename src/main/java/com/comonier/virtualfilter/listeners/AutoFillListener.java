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
        boolean isEdit = (targetItem.hasItemMeta() && targetItem.getItemMeta().hasDisplayName());
        String customName = isEdit ? targetItem.getItemMeta().getDisplayName() : null;

        VirtualFilter.getInstance().getServer().getScheduler().runTask(VirtualFilter.getInstance(), () -> {
            ItemStack currentHand = player.getInventory().getItemInMainHand();
            
            if (null != currentHand && currentHand.getType() != Material.AIR) {
                return;
            }

            for (int i = 0; 36 > i; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (null != invItem && invItem.getType() == type) {
                    if (isEdit) {
                        if (invItem.hasItemMeta() && customName.equals(invItem.getItemMeta().getDisplayName())) {
                            player.getInventory().setItemInMainHand(invItem);
                            player.getInventory().setItem(i, null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                            return;
                        }
                    } else {
                        if (false == (invItem.hasItemMeta() && invItem.getItemMeta().hasDisplayName()) && invItem.getEnchantments().isEmpty()) {
                            player.getInventory().setItemInMainHand(invItem);
                            player.getInventory().setItem(i, null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                            return;
                        }
                    }
                }
            }

            if (type.getMaxStackSize() > 1) {
                int amountTaken = 0;
                if (isEdit) {
                    amountTaken = VirtualFilter.getInstance().getFilterEditRepo().withdrawFromISFE(uuid, type.name(), customName, 64);
                } else {
                    amountTaken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, type.name(), 64);
                }

                if (amountTaken > 0) {
                    ItemStack newItem = new ItemStack(type, amountTaken);
                    if (isEdit) {
                        ItemMeta meta = newItem.getItemMeta();
                        if (null != meta) {
                            meta.setDisplayName(customName);
                            newItem.setItemMeta(meta);
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
