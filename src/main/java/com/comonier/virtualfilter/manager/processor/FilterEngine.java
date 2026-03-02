package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.UUID;

public class FilterEngine {

    private final ReportManager reportManager;

    public FilterEngine(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    /**
     * Processa a hierarquia principal: AutoSell > InfinityStack > AutoBlock > Inventario
     */
    public boolean process(Player player, ItemStack item) {
        // Validacoes basicas de seguranca
        if (null == item || item.getType() == Material.AIR) return false;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return false;

        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();

        // 1. Hierarquia ASF (AutoSell)
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "asf", mat)) {
            double price = ShopGUIHook.getItemPrice(player, item);
            VirtualFilter.getEconomy().depositPlayer(player, price * item.getAmount());
            reportManager.logReport(player, mat, item.getAmount(), "VENDIDO (ASF)");
            return true; 
        }

        // 2. Hierarquia ISF (InfinityStack)
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "isf", mat)) {
            VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, mat, (long) item.getAmount());
            reportManager.logReport(player, mat, item.getAmount(), "ESTOCADO (ISF)");
            return true;
        }

        // 3. Hierarquia ABF (AutoBlock)
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "abf", mat)) {
            reportManager.logReport(player, mat, item.getAmount(), "CHÃO (ABF)");
            return false; 
        }

        // 4. Entrega final no Inventario Fisico
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
        
        // Logica inversa: se o tamanho for 0 ou menos, tudo coube no inventario
        if (0 >= left.size()) {
            reportManager.logReport(player, mat, item.getAmount(), "INVENTÁRIO");
            return true;
        }

        // --- PADRONIZACAO: ALERTA DE INVENTARIO CHEIO (MISSÃO ALERTA TOTAL) ---
        // Se chegou aqui, e porque sobrou item (Inventario Cheio)
        player.sendMessage("§6§l[VF] §c§lFULL INVENTORY!");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        
        reportManager.logReport(player, mat, item.getAmount(), "CHÃO (INV CHEIO)");
        return false;
    }
}
