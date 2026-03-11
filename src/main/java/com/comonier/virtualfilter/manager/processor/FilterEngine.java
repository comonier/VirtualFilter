package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.UUID;

public class FilterEngine {
    private final ReportManager reportManager;

    public FilterEngine(ReportManager reportManager) { 
        this.reportManager = reportManager; 
    }

    public boolean process(Player player, ItemStack item) {
        if (null == item || item.getType() == Material.AIR) {
            return false;
        }

        if (item.getType().name().contains("SHULKER_BOX")) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
        ItemMeta meta = item.getItemMeta();

        // --- NOVA CAMADA: ISFE (Itens Editados) ---
        // Se o item tem nome customizado, ele ignora ASF/ISF/ABF e tenta o ISFE
        if (null != meta && meta.hasDisplayName()) {
            String customName = meta.getDisplayName();
            if (VirtualFilter.getInstance().getFilterEditRepo().hasFilter(uuid, "isfe", mat, customName)) {
                VirtualFilter.getInstance().getFilterEditRepo().addAmount(uuid, mat, customName, (long) item.getAmount());
                reportManager.logReport(player, customName, item.getAmount(), "log_dest_isf");
                return true;
            }
            // Se for editado mas NÃO tiver filtro ISFE, segue para o inventário normal (Fallback)
        } else {
            // --- FLUXO VANILLA (Itens sem nome customizado) ---
            
            // 1. ASF - AutoSell
            if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "asf", mat)) {
                double price = ShopGUIHook.getItemPrice(player, item);
                if (price > 0.0) {
                    double total = price * item.getAmount();
                    VirtualFilter.getEconomy().depositPlayer(player, total);
                    
                    if (VirtualFilter.getInstance().getSettingsRepo().isActionBarEnabled(uuid)) {
                        String abMsg = VirtualFilter.getInstance().getMsg(lang, "asf_actionbar")
                                .replace("%price%", String.format("%.2f", total))
                                .replace("%amount%", String.valueOf(item.getAmount()))
                                .replace("%item%", mat);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(abMsg));
                    }
                    
                    reportManager.logReport(player, mat, item.getAmount(), "log_dest_asf");
                    return true;
                }
            }

            // 2. ISF - InfinityStack
            if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "isf", mat)) {
                VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, mat, (long) item.getAmount());
                reportManager.logReport(player, mat, item.getAmount(), "log_dest_isf");
                return true;
            }

            // 3. ABF - AutoBlock
            if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "abf", mat)) {
                reportManager.logReport(player, mat, item.getAmount(), "log_dest_abf");
                return false;
            }
        }

        // Fallback: Inventário normal para Vanilla ou Editados sem filtro
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
        if (left.isEmpty()) {
            String reportName = (null != meta && meta.hasDisplayName()) ? meta.getDisplayName() : mat;
            reportManager.logReport(player, reportName, item.getAmount(), "log_dest_inv");
            return true;
        }

        player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "inv_full"));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        
        String finalName = (null != meta && meta.hasDisplayName()) ? meta.getDisplayName() : mat;
        reportManager.logReport(player, finalName, item.getAmount(), "log_dest_full");
        
        return false;
    }
}
