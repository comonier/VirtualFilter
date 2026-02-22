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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class FilterProcessor implements Listener {

    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        event.getItemDrop().setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (processItem(player, item)) {
            event.getItem().remove();
            event.setCancelled(true);
            playTeleportSound(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
            event.setDropItems(false); 
            event.getBlock().getDrops(player.getInventory().getItemInMainHand(), player).forEach(drop -> {
                deliverItem(player, drop, event.getBlock().getLocation(), false);
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        if (itemEntity.hasMetadata("drop_time")) {
            long dropTime = itemEntity.getMetadata("drop_time").get(0).asLong();
            long diff = System.currentTimeMillis() - dropTime;
            if (5001 > diff) return;
        }
        for (Entity entity : itemEntity.getNearbyEntities(10.0, 10.0, 10.0)) {
            if (entity instanceof Player player) {
                if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
                    ItemStack stack = itemEntity.getItemStack();
                    event.setCancelled(true);
                    deliverItem(player, stack, itemEntity.getLocation(), true);
                    return;
                }
            }
        }
    }

    private void deliverItem(Player player, ItemStack item, org.bukkit.Location loc, boolean shouldPlaySound) {
        if (!processItem(player, item)) {
            HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
            if (!left.isEmpty()) {
                for (ItemStack remaining : left.values()) {
                    player.getWorld().dropItemNaturally(loc, remaining);
                }
            }
        }
        if (shouldPlaySound) playTeleportSound(player);
    }

    private void playTeleportSound(Player player) {
        long now = System.currentTimeMillis();
        long lastPlay = soundCooldowns.getOrDefault(player.getUniqueId(), 0L);
        // COOLDOWN ATUALIZADO: 2000ms = 2 segundos
        if (now - lastPlay > 2000) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);
            soundCooldowns.put(player.getUniqueId(), now);
        }
    }

    public boolean processItem(Player player, ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return false;
        String mat = item.getType().name();
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());

        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "asf", mat)) {
            double price = ShopGUIHook.getItemPrice(player, item);
            double total = price * item.getAmount();
            if (VirtualFilter.getEconomy() != null) {
                VirtualFilter.getEconomy().depositPlayer(player, total);
                if (VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId())) {
                    String msg = VirtualFilter.getInstance().getMsg(lang, "asf_actionbar")
                        .replace("%price%", String.format("%.2f", total))
                        .replace("%amount%", String.valueOf(item.getAmount()))
                        .replace("%item%", mat);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                }
            }
            return true;
        }
        if (VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "isf", mat)) {
            VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), mat, item.getAmount());
            return true;
        }
        return VirtualFilter.getInstance().getDbManager().hasFilter(player.getUniqueId(), "abf", mat);
    }
}
