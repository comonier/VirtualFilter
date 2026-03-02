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
        if (false == title.contains("Filter")) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == player.getInventory()) return;

        int slot = event.getSlot();
        if (0 > slot || slot >= 54) return;

        String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");
        UUID uuid = player.getUniqueId();
        ItemStack clicked = event.getCurrentItem();

        if (null == clicked || clicked.getType() == Material.AIR || clicked.getType().name().contains("GLASS_PANE")) return;

        String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, typeCode, slot);
        if (null == matName) return;

        // --- SAQUE MASSIVO (SHIFT + ESQUERDO) ---
        if (typeCode.equals("isf") && event.getClick() == ClickType.SHIFT_LEFT) {
            long before = VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName);
            VirtualFilter.getInstance().getFilterRepo().withdrawMassive(player, matName);
            long after = VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName);
            
            long withdrawn = before - after;
            if (withdrawn > 0) {
                player.sendMessage("§6[VF] §aWithdrawn §f" + withdrawn + "x §e" + matName);
            }

            if (0 >= after) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, typeCode, slot);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            
            sync(player, typeCode);
            return;
        }

        // --- SAQUE 1 PACK (CLIQUE DIREITO) ---
        if (typeCode.equals("isf") && event.getClick() == ClickType.RIGHT) {
            int taken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, matName, 64);
            if (taken > 0) {
                Material m = Material.getMaterial(matName);
                if (null != m) {
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(m, taken));
                    int kept = taken;
                    if (false == left.isEmpty()) {
                        kept = taken - left.get(0).getAmount();
                        for (ItemStack overflow : left.values()) {
                            VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) overflow.getAmount());
                        }
                        player.sendMessage("§6[VF] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    if (kept > 0) {
                        player.sendMessage("§6[VF] §aWithdrawn §f" + kept + "x §e" + matName);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    }
                }
            }
            sync(player, typeCode);
            return;
        }

        // --- REMOCAO (SHIFT + DIREITO) ---
        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            if (typeCode.equals("isf")) {
                if (VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName) > 0) {
                    player.sendMessage("§6[VF] §c§lERROR: §fEsvazie o estoque primeiro!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
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
