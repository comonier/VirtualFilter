package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockLootListener implements Listener {
    private final FilterEngine engine;
    private final ReportManager reportManager;
    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();

    public BlockLootListener(FilterEngine engine, ReportManager reportManager) { 
        this.engine = engine; 
        this.reportManager = reportManager; 
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean al = VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(uuid);
        List<Item> toRemove = new ArrayList<>();

        for (Item itemEntity : event.getItems()) {
            ItemStack stack = itemEntity.getItemStack();
            
            if (stack.getType().name().contains("SHULKER_BOX")) {
                // 1. Tenta Inventário
                HashMap<Integer, ItemStack> leftInv = player.getInventory().addItem(stack);
                if (leftInv.isEmpty()) {
                    toRemove.add(itemEntity);
                    reportManager.logReport(player, stack.getType().name(), 1, "log_dest_inv");
                    playTeleportSound(player);
                    continue;
                }

                // 2. Tenta Ender Chest
                HashMap<Integer, ItemStack> leftEnder = player.getEnderChest().addItem(stack);
                if (leftEnder.isEmpty()) {
                    toRemove.add(itemEntity);
                    reportManager.logReport(player, stack.getType().name(), 1, "log_dest_ender");
                    player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.2f);
                    continue;
                }

                // 3. Fallback: Chão
                reportManager.logReport(player, stack.getType().name(), 1, "log_dest_full");
                continue;
            }

            itemEntity.setMetadata("item_owner", new FixedMetadataValue(VirtualFilter.getInstance(), uuid.toString()));
            itemEntity.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));

            if (al) {
                if (engine.process(player, stack)) {
                    toRemove.add(itemEntity);
                }
            }
        }
        
        int i = 0;
        while (toRemove.size() > i) {
            toRemove.get(i).remove();
            i++;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getState() instanceof ShulkerBox) return;

        if (event.getBlock().getState() instanceof Container) {
            Container container = (Container) event.getBlock().getState();
            if (VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(player.getUniqueId())) {
                for (ItemStack item : container.getInventory().getContents()) {
                    if (null != item && item.getType() != Material.AIR) {
                        if (engine.process(player, item)) {
                            playTeleportSound(player);
                        } else {
                            String mat = item.getType().name();
                            if (false == VirtualFilter.getInstance().getFilterRepo().hasFilter(player.getUniqueId(), "abf", mat)) {
                                player.getInventory().addItem(item);
                            } else {
                                player.getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
                            }
                        }
                    }
                }
                container.getInventory().clear();
            }
        }
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
