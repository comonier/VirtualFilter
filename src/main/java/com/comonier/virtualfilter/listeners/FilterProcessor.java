package com.comonier.virtualfilter.listeners;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
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
import java.util.Map;
import java.util.UUID;

public class FilterProcessor implements Listener {

    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();
    private final HashMap<UUID, Map<String, Integer>> chestReport = new HashMap<>();

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
        Block block = event.getBlock();
        
        if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
            
            if (block.getState() instanceof Container container) {
                event.setDropItems(false);
                
                // 1. Processa o CONTEÚDO do baú
                for (ItemStack content : container.getInventory().getContents()) {
                    if (content != null && content.getType() != Material.AIR) {
                        deliverItemWithGroupedReport(player, content.clone(), block.getLocation());
                    }
                }
                container.getInventory().clear();

                // 2. Processa o BLOCO do baú (Garante que ele não seja destruído/perdido)
                ItemStack chestItem = new ItemStack(block.getType(), 1);
                deliverItemWithGroupedReport(player, chestItem, block.getLocation());

            } else {
                // Mineração comum
                event.setDropItems(false); 
                block.getDrops(player.getInventory().getItemInMainHand(), player).forEach(drop -> {
                    deliverItemWithGroupedReport(player, drop, block.getLocation());
                });
            }
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

        for (Entity entity : itemEntity.getNearbyEntities(11.0, 11.0, 11.0)) {
            if (entity instanceof Player player) {
                if (VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId())) {
                    ItemStack stack = itemEntity.getItemStack();
                    Material t = stack.getType();
                    // Ignora containers no spawn para evitar conflito com o BlockBreak
                    if (t == Material.CHEST || t == Material.BARREL || t.name().contains("SHULKER")) return;

                    event.setCancelled(true);
                    deliverItemWithGroupedReport(player, stack, itemEntity.getLocation());
                    return;
                }
            }
        }
    }

    private void deliverItemWithGroupedReport(Player player, ItemStack item, org.bukkit.Location loc) {
        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();
        int amount = item.getAmount();
        boolean debug = VirtualFilter.getInstance().getDbManager().isChestDebugEnabled(uuid);

        String destination = "CHÃO";

        if (VirtualFilter.getInstance().getDbManager().hasFilter(uuid, "isf", mat)) {
            VirtualFilter.getInstance().getDbManager().addAmount(uuid, mat, amount);
            destination = "ISF";
            playTeleportSound(player);
        } else {
            ItemStack toAdd = item.clone();
            HashMap<Integer, ItemStack> left = player.getInventory().addItem(toAdd);
            
            if (0 >= left.size()) {
                destination = "INV";
                playTeleportSound(player);
            } else {
                for (ItemStack remaining : left.values()) {
                    if (remaining != null && remaining.getAmount() > 0) {
                        player.getWorld().dropItemNaturally(loc, remaining);
                    }
                }
                destination = "INV/CHÃO";
            }
        }

        if (debug) {
            String reportKey = mat + " -> " + destination;
            chestReport.computeIfAbsent(uuid, k -> new HashMap<>());
            Map<String, Integer> playerMap = chestReport.get(uuid);
            playerMap.put(reportKey, playerMap.getOrDefault(reportKey, 0) + amount);

            VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                Map<String, Integer> summary = chestReport.remove(uuid);
                if (summary != null) {
                    player.sendMessage("§6[VirtualFilter] Coleta:");
                    summary.forEach((info, total) -> {
                        String color = info.contains("ISF") ? "§3" : info.contains("INV") ? "§c" : "§5";
                        player.sendMessage(color + "• " + total + "x " + info);
                    });
                }
            }, 3L);
        }
    }

    private void playTeleportSound(Player player) {
        long now = System.currentTimeMillis();
        long lastPlay = soundCooldowns.getOrDefault(player.getUniqueId(), 0L);
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
