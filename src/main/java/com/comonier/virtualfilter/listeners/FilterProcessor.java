package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class FilterProcessor implements Listener {

    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();
    private final HashMap<UUID, Map<String, Integer>> chestReport = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        item.setMetadata("manual_drop", new FixedMetadataValue(VirtualFilter.getInstance(), true));
        item.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        // Correção de símbolo: Cast tradicional
        if (false == (event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        
        Item entity = event.getItem();
        UUID picker = player.getUniqueId();

        if (entity.hasMetadata("manual_drop")) {
            return;
        }

        if (entity.hasMetadata("item_owner")) {
            UUID owner = UUID.fromString(entity.getMetadata("item_owner").get(0).asString());
            if (false == picker.equals(owner)) {
                long spawnTime = entity.getMetadata("drop_time").get(0).asLong();
                long diff = System.currentTimeMillis() - spawnTime;
                if (10000 > diff) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (processLootSequence(player, entity.getItemStack(), "PICKUP")) {
            entity.remove();
            event.setCancelled(true);
            playTeleportSound(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (false == VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item itemEntity = (Item) event.getCaught();
            if (processLootSequence(player, itemEntity.getItemStack(), "FISHING")) {
                itemEntity.remove();
                playTeleportSound(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean al = VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(uuid);
        List<Item> toRemove = new ArrayList<>();

        for (Item itemEntity : event.getItems()) {
            itemEntity.setMetadata("item_owner", new FixedMetadataValue(VirtualFilter.getInstance(), uuid.toString()));
            itemEntity.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
            if (al) {
                if (processLootSequence(player, itemEntity.getItemStack(), "AUTOLOOT")) {
                    toRemove.add(itemEntity);
                }
            }
        }
        for (Item item : toRemove) {
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (null == killer) {
            return;
        }

        UUID uuid = killer.getUniqueId();
        boolean al = VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(uuid);

        if (al) {
            List<ItemStack> drops = event.getDrops();
            Iterator<ItemStack> it = drops.iterator();
            while (it.hasNext()) {
                ItemStack drop = it.next();
                if (processLootSequence(killer, drop, "MOB_LOOT")) {
                    it.remove();
                    playTeleportSound(killer);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getState() instanceof Container) {
            Container container = (Container) event.getBlock().getState();
            if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
                for (ItemStack item : container.getInventory().getContents()) {
                    if (null != item && item.getType() != Material.AIR) {
                        deliverItemWithReport(player, item.clone(), event.getBlock().getLocation());
                    }
                }
                container.getInventory().clear();
            }
        }
    }

    private boolean deliverItemWithReport(Player player, ItemStack item, org.bukkit.Location loc) {
        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();
        
        if (processLootSequence(player, item, "CHEST_COLLECTION")) {
            return true;
        }

        if (VirtualFilter.getInstance().getDbManager().hasFilter(uuid, "abf", mat)) {
            logReport(player, mat, item.getAmount(), "CHÃO (ABF)");
            player.getWorld().dropItemNaturally(loc, item);
            return false;
        }

        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
        if (0 >= left.size()) {
            logReport(player, mat, item.getAmount(), "INVENTÁRIO");
            playTeleportSound(player);
            return true;
        }

        for (ItemStack remaining : left.values()) {
            player.getWorld().dropItemNaturally(loc, remaining);
        }
        logReport(player, mat, item.getAmount(), "CHÃO (INV CHEIO)");
        return false;
    }

    public boolean processLootSequence(Player player, ItemStack item, String context) {
        if (null == item || item.getType() == Material.AIR) {
            return false;
        }

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();

        if (VirtualFilter.getInstance().getDbManager().hasFilter(uuid, "asf", mat)) {
            double price = ShopGUIHook.getItemPrice(player, item);
            VirtualFilter.getEconomy().depositPlayer(player, price * item.getAmount());
            logReport(player, mat, item.getAmount(), "VENDIDO (ASF)");
            return true; 
        }

        if (VirtualFilter.getInstance().getDbManager().hasFilter(uuid, "isf", mat)) {
            VirtualFilter.getInstance().getDbManager().addAmount(uuid, mat, (long) item.getAmount());
            logReport(player, mat, item.getAmount(), "ESTOCADO (ISF)");
            return true;
        }

        if (VirtualFilter.getInstance().getDbManager().hasFilter(uuid, "abf", mat)) {
            logReport(player, mat, item.getAmount(), "CHÃO (ABF)");
            return false; 
        }

        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
        if (0 >= left.size()) {
            logReport(player, mat, item.getAmount(), "INVENTÁRIO");
            return true;
        }

        return false;
    }

    private void logReport(Player player, String mat, int amount, String destination) {
        if (false == VirtualFilter.getInstance().getDbManager().isChestDebugEnabled(player.getUniqueId())) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        String entry = mat + " -> " + destination;
        chestReport.computeIfAbsent(uuid, k -> new HashMap<>());
        chestReport.get(uuid).put(entry, chestReport.get(uuid).getOrDefault(entry, 0) + amount);

        VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
            Map<String, Integer> summary = chestReport.remove(uuid);
            if (null != summary) {
                String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(uuid);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "chest_report_title"));
                summary.forEach((info, total) -> player.sendMessage(" §b• " + total + "x " + info));
            }
        }, 5L);
    }

    private void playTeleportSound(Player player) {
        long now = System.currentTimeMillis();
        long lastSound = soundCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastSound > 2000) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);
            soundCooldowns.put(player.getUniqueId(), now);
        }
    }
}
