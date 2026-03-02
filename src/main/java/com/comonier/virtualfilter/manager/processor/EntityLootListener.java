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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityLootListener implements Listener {

    private final FilterEngine engine;
    private final HashMap<UUID, Long> soundCooldowns = new HashMap<>();

    public EntityLootListener(FilterEngine engine) {
        this.engine = engine;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        // Marca o item para o filtro ignorar (proteção contra coleta acidental)
        item.setMetadata("manual_drop", new FixedMetadataValue(VirtualFilter.getInstance(), true));
        item.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (false == (event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Item entity = event.getItem();
        UUID picker = player.getUniqueId();

        // Se o item foi dropado manualmente, o filtro não interfere
        if (entity.hasMetadata("manual_drop")) return;

        // Proteção de Loot (Missão: Respeitar o dono do drop por 10s)
        if (entity.hasMetadata("item_owner")) {
            UUID owner = UUID.fromString(entity.getMetadata("item_owner").get(0).asString());
            if (false == picker.equals(owner)) {
                long spawnTime = entity.getMetadata("drop_time").get(0).asLong();
                long diff = System.currentTimeMillis() - spawnTime;
                // Inversão: 10000ms (10s) > diff para manter bloqueado
                if (10000 > diff) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Tenta processar o item coletado pela hierarquia de filtros
        if (engine.process(player, entity.getItemStack())) {
            entity.remove();
            event.setCancelled(true);
            playTeleportSound(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        // Verifica se AutoLoot está ativo no repositório modular
        if (false == VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(player.getUniqueId())) return;

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item itemEntity = (Item) event.getCaught();
            if (engine.process(player, itemEntity.getItemStack())) {
                itemEntity.remove();
                playTeleportSound(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (null == killer) return;

        if (VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(killer.getUniqueId())) {
            List<ItemStack> drops = event.getDrops();
            Iterator<ItemStack> it = drops.iterator();
            while (it.hasNext()) {
                ItemStack drop = it.next();
                // Processa cada drop do mob pela hierarquia do motor
                if (engine.process(killer, drop)) {
                    it.remove();
                    playTeleportSound(killer);
                }
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
