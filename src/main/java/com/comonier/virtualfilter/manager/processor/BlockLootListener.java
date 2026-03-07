package com.comonier.virtualfilter.manager.processor;
import com.comonier.virtualfilter.VirtualFilter;
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
    public BlockLootListener(FilterEngine engine, ReportManager reportManager) { this.engine = engine; this.reportManager = reportManager; }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean al = VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(uuid);
        List<Item> toRemove = new ArrayList<>();
        for (Item itemEntity : event.getItems()) {
            itemEntity.setMetadata("item_owner", new FixedMetadataValue(VirtualFilter.getInstance(), uuid.toString()));
            itemEntity.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
            if (al) {
                if (engine.process(player, itemEntity.getItemStack())) {
                    toRemove.add(itemEntity);
                }
            }
        }
        for (Item item : toRemove) item.remove();
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getState() instanceof Container) {
            Container container = (Container) event.getBlock().getState();
            if (VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(player.getUniqueId())) {
                for (ItemStack item : container.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        if (engine.process(player, item)) {
                            playTeleportSound(player);
                        } else {
                            String mat = item.getType().name();
                            if (!VirtualFilter.getInstance().getFilterRepo().hasFilter(player.getUniqueId(), "abf", mat)) {
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
