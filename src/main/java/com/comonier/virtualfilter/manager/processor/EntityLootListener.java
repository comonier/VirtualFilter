package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class EntityLootListener implements Listener {
    private final FilterEngine engine;
    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();

    public EntityLootListener(FilterEngine engine) { this.engine = engine; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemEntity = event.getItemDrop();
        ItemStack stack = itemEntity.getItemStack();
        UUID uuid = player.getUniqueId();
        
        itemEntity.setMetadata("manual_drop", new FixedMetadataValue(VirtualFilter.getInstance(), true));
        itemEntity.setMetadata("drop_owner", new FixedMetadataValue(VirtualFilter.getInstance(), uuid.toString()));
        itemEntity.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));

        new BukkitRunnable() {
            int timer = 10;
            @Override
            public void run() {
                if (false == itemEntity.isValid() || itemEntity.isDead()) { this.cancel(); return; }
                
                if (timer > 0) {
                    // Só envia a mensagem se o SafeDrop ainda estiver ativado para o jogador
                    if (VirtualFilter.getInstance().getSettingsRepo().isSafeDropEnabled(uuid)) {
                        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
                        String msg = VirtualFilter.getInstance().getMsg(lang, "safedrop_countdown")
                                .replace("%item%", stack.getType().name())
                                .replace("%time%", String.valueOf(timer));
                        player.sendMessage(msg);
                    }
                    timer--;
                } else {
                    handleAutoReturn(player, itemEntity);
                    this.cancel();
                }
            }
        }.runTaskTimer(VirtualFilter.getInstance(), 20L, 20L);
    }

    private void handleAutoReturn(Player player, Item itemEntity) {
        if (false == itemEntity.isValid()) return;
        ItemStack stack = itemEntity.getItemStack();

        if (stack.getType().name().contains("SHULKER_BOX")) {
            if (player.getInventory().addItem(stack).isEmpty()) {
                itemEntity.remove();
                VirtualFilter.getInstance().getReportManager().logReport(player, stack.getType().name(), 1, "log_dest_inv");
                playTeleportSound(player);
                return;
            }
            if (player.getEnderChest().addItem(stack).isEmpty()) {
                itemEntity.remove();
                VirtualFilter.getInstance().getReportManager().logReport(player, stack.getType().name(), 1, "log_dest_ender");
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.2f);
                return;
            }
            VirtualFilter.getInstance().getReportManager().logReport(player, stack.getType().name(), 1, "log_dest_full");
            return;
        }

        if (engine.process(player, stack)) {
            itemEntity.remove();
            playTeleportSound(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (false == (event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Item entity = event.getItem();
        UUID uuid = player.getUniqueId();

        if (entity.hasMetadata("drop_owner")) {
            UUID owner = UUID.fromString(entity.getMetadata("drop_owner").get(0).asString());
            
            if (VirtualFilter.getInstance().getSettingsRepo().isSafeDropEnabled(owner)) {
                long dropTime = entity.getMetadata("drop_time").get(0).asLong();
                long diff = System.currentTimeMillis() - dropTime;

                if (10000 > diff) {
                    if (false == uuid.equals(owner)) {
                        event.setCancelled(true);
                        return;
                    }
                    return;
                }
            }
        }

        if (false == entity.hasMetadata("manual_drop")) {
            if (engine.process(player, entity.getItemStack())) {
                entity.remove();
                event.setCancelled(true);
                playTeleportSound(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (false == VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(player.getUniqueId())) return;
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item itemEntity = (Item) event.getCaught();
            if (engine.process(player, itemEntity.getItemStack())) {
                itemEntity.remove(); playTeleportSound(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (null == killer) return;
        if (VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(killer.getUniqueId())) {
            Iterator<ItemStack> it = event.getDrops().iterator();
            while (it.hasNext()) {
                ItemStack drop = it.next();
                if (engine.process(killer, drop)) { it.remove(); playTeleportSound(killer); }
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
