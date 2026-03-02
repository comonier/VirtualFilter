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

    public BlockLootListener(FilterEngine engine, ReportManager reportManager) {
        this.engine = engine;
        this.reportManager = reportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Verifica se AutoLoot está ativado via SettingsRepo
        boolean al = VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(uuid);
        List<Item> toRemove = new ArrayList<>();

        for (Item itemEntity : event.getItems()) {
            // Marca o dono do drop para proteção de 10s
            itemEntity.setMetadata("item_owner", new FixedMetadataValue(VirtualFilter.getInstance(), uuid.toString()));
            itemEntity.setMetadata("drop_time", new FixedMetadataValue(VirtualFilter.getInstance(), System.currentTimeMillis()));
            
            if (al) {
                if (engine.process(player, itemEntity.getItemStack())) {
                    toRemove.add(itemEntity);
                }
            }
        }
        
        // Limpa as entidades de item que foram filtradas
        for (Item item : toRemove) {
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Lógica para coletar itens de baús/containers ao quebrá-los (vfcb)
        if (event.getBlock().getState() instanceof Container) {
            Container container = (Container) event.getBlock().getState();
            if (VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(player.getUniqueId())) {
                for (ItemStack item : container.getInventory().getContents()) {
                    if (null != item && item.getType() != Material.AIR) {
                        deliverWithLogic(player, item.clone(), event.getBlock().getLocation());
                    }
                }
                container.getInventory().clear();
            }
        }
    }

    private void deliverWithLogic(Player player, ItemStack item, org.bukkit.Location loc) {
        // Tenta processar pelo motor de filtros
        if (engine.process(player, item)) {
            playTeleportSound(player);
            return;
        }

        // Se falhar (ex: ABF ativado ou inventário cheio), dropa no chão com log
        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();

        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "abf", mat)) {
            reportManager.logReport(player, mat, item.getAmount(), "CHÃO (ABF)");
            player.getWorld().dropItemNaturally(loc, item);
        } else {
            // Tentativa final de adicionar ao inventário físico
            HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
            if (0 >= left.size()) {
                reportManager.logReport(player, mat, item.getAmount(), "INVENTÁRIO");
                playTeleportSound(player);
            } else {
                for (ItemStack remaining : left.values()) {
                    player.getWorld().dropItemNaturally(loc, remaining);
                }
                reportManager.logReport(player, mat, item.getAmount(), "CHÃO (INV CHEIO)");
            }
        }
    }

    private void playTeleportSound(Player player) {
        long now = System.currentTimeMillis();
        long lastSound = soundCooldowns.getOrDefault(player.getUniqueId(), 0L);
        // Cooldown de 2s para não floodar sons
        if (now - lastSound > 2000) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);
            soundCooldowns.put(player.getUniqueId(), now);
        }
    }
}
