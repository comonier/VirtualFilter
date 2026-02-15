package com.comonier.virtualfilter.integration;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;

public class ShopGUIHook {
    private static FileConfiguration prices;

    public static void loadPrices() {
        File file = new File(VirtualFilter.getInstance().getDataFolder(), "prices.yml");
        if (!file.exists()) VirtualFilter.getInstance().saveResource("prices.yml", false);
        prices = YamlConfiguration.loadConfiguration(file);
    }

    public static double getItemPrice(Player player, ItemStack item) {
        String material = item.getType().name();
        
        // Se o item NÃO estiver configurado no prices.yml
        if (!prices.contains(material)) {
            // Alterado de 1.0 para 0.1 para evitar que itens lixo valham mais que minerios
            return 0.1;
        }
        
        return prices.getDouble(material);
    }
}
