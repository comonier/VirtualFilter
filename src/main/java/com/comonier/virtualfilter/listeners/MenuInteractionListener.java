package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.UUID;

public class MenuInteractionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (false == (event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (false == (title.contains("Filter") && false == title.contains("Edited"))) return;
        
        event.setCancelled(true);
        if (null == event.getClickedInventory() || event.getClickedInventory() == player.getInventory()) return;
        
        int slot = event.getSlot();
        if (0 > slot || slot >= 54) return;
        
        String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");
        UUID uuid = player.getUniqueId();
        ItemStack clicked = event.getCurrentItem();
        
        if (null == clicked || clicked.getType() == Material.AIR || clicked.getType().name().contains("GLASS_PANE")) return;
        
        String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, typeCode, slot);
        if (null == matName) return;
        
        if (typeCode.equals("isf") && event.getClick() == ClickType.SHIFT_LEFT) {
            VirtualFilter.getInstance().getFilterRepo().withdrawMassive(player, matName);
            if (0 >= VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName)) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, typeCode, slot);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            sync(player, typeCode);
            return;
        }
        
        if (typeCode.equals("isf") && event.getClick() == ClickType.RIGHT) {
            int taken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, matName, 64);
            if (taken > 0) {
                Material m = Material.getMaterial(matName);
                if (null != m) {
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(m, taken));
                    if (false == left.isEmpty()) {
                        int remaining = 0;
                        for (ItemStack s : left.values()) remaining += s.getAmount();
                        VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) remaining);
                        player.sendMessage("§6[VF] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
            }
            sync(player, typeCode);
            return;
        }
        
        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            if (typeCode.equals("isf") && 0 < VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName)) {
                String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "delete_blocked_isf"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, typeCode, slot);
            sync(player, typeCode);
        }
    }

    private void sync(Player player, String type) {
        VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
            player.updateInventory();
            FilterMenu.open(player, type);
        }, 2L);
    }
}
