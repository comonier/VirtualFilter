package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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

    public boolean process(Player player, ItemStack item) {
        if (null == item || item.getType() == Material.AIR) {
            return false;
        }

        // TRAVA SUPREMA: Shulker Boxes são ignoradas por todos os filtros (ASF/ISF/ABF)
        // Isso garante que o BlockLootListener assuma o controle total sobre elas.
        if (item.getType().name().contains("SHULKER_BOX")) {
            return false;
        }

        // Ignora itens com nomes customizados (ferramentas, itens de RPG, etc)
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String mat = item.getType().name();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        // 1. ASF - AutoSell (Verifica se está no filtro e se tem preço)
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

        // 2. ISF - InfinityStack (Armazenamento Virtual)
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "isf", mat)) {
            VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, mat, (long) item.getAmount());
            reportManager.logReport(player, mat, item.getAmount(), "log_dest_isf");
            return true;
        }

        // 3. ABF - AutoBlock (Deleta o drop se estiver no filtro)
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, "abf", mat)) {
            reportManager.logReport(player, mat, item.getAmount(), "log_dest_abf");
            return false;
        }

        // Fallback: Tenta adicionar ao inventário normal
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item.clone());
        if (left.isEmpty()) {
            reportManager.logReport(player, mat, item.getAmount(), "log_dest_inv");
            return true;
        }

        // Se falhou (inventário cheio), o item cai no chão
        player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "inv_full"));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        reportManager.logReport(player, mat, item.getAmount(), "log_dest_full");
        
        return false;
    }
}
