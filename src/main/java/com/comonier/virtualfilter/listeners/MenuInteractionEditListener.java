package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterEditMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.UUID;

public class MenuInteractionEditListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (false == (event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (false == title.contains("InfinityStackFilterEdited")) return;
        
        event.setCancelled(true);
        if (null == event.getClickedInventory() || event.getClickedInventory() == player.getInventory()) return;

        int slot = event.getSlot();
        if (0 > slot || slot >= 54) return;

        UUID uuid = player.getUniqueId();
        ItemStack clicked = event.getCurrentItem();
        
        if (null == clicked || clicked.getType() == Material.AIR || clicked.getType().name().contains("GLASS_PANE")) return;

        String[] data = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, "isfe", slot);
        if (null == data) return;

        String matName = data[0];
        String customName = data[1];

        if (event.getClick() == ClickType.SHIFT_LEFT) {
            VirtualFilter.getInstance().getFilterEditRepo().withdrawMassive(player, matName, customName);
            
            if (0 >= VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(uuid, matName, customName)) {
                VirtualFilter.getInstance().getFilterEditRepo().removeAndShift(uuid, "isfe", slot);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            sync(player);
            return;
        }

        if (event.getClick() == ClickType.RIGHT) {
            int taken = VirtualFilter.getInstance().getFilterEditRepo().withdrawFromISFE(uuid, matName, customName, 64);
            if (taken > 0) {
                Material m = Material.getMaterial(matName);
                if (null != m) {
                    ItemStack itemToGive = new ItemStack(m, taken);
                    ItemMeta meta = itemToGive.getItemMeta();
                    if (null != meta) {
                        meta.setDisplayName(customName);
                        itemToGive.setItemMeta(meta);
                    }

                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(itemToGive);
                    if (false == left.isEmpty()) {
                        int remaining = 0;
                        for (ItemStack s : left.values()) remaining += s.getAmount();
                        VirtualFilter.getInstance().getFilterEditRepo().addAmount(uuid, matName, customName, (long) remaining);
                        player.sendMessage("§6[VFE] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
            }
            sync(player);
            return;
        }

        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            if (0 < VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(uuid, matName, customName)) {
                String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "delete_blocked_isf"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            VirtualFilter.getInstance().getFilterEditRepo().removeAndShift(uuid, "isfe", slot);
            sync(player);
        }
    }

    private void sync(Player player) {
        VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
            player.updateInventory();
            FilterEditMenu.open(player);
        }, 2L);
    }
}
