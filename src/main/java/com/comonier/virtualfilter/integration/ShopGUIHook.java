package com.comonier.virtualfilter.integration;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class ShopGUIHook {

    private static FileConfiguration priceConfig;

    /**
     * Carrega os preços do arquivo prices.yml interno do plugin.
     */
    public static void loadPrices() {
        File file = new File(VirtualFilter.getInstance().getDataFolder(), "prices.yml");
        if (!file.exists()) {
            VirtualFilter.getInstance().saveResource("prices.yml", false);
        }
        priceConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Obtém o preço de venda de um item definido no seu prices.yml.
     * Mantivemos a assinatura (Player, ItemStack) para não quebrar seus outros arquivos.
     */
    public static double getItemPrice(Player player, ItemStack item) {
        if (priceConfig == null) loadPrices();
        
        String materialName = item.getType().name();
        // Busca o preço no prices.yml. Se não achar, retorna -1.0
        return priceConfig.getDouble(materialName, -1.0);
    }
    
    /**
     * Agora sempre retorna true, pois o "sistema de preços" é interno do seu plugin.
     */
    public static boolean isEnabled() {
        return true;
    }
}
