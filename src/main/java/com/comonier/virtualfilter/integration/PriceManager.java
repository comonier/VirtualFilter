package com.comonier.virtualfilter.integration;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import java.io.File;

public class PriceManager {
    private FileConfiguration priceConfig;

    public void loadPrices() {
        File file = new File(VirtualFilter.getInstance().getDataFolder(), "prices.yml");
        if (!file.exists()) {
            VirtualFilter.getInstance().saveResource("prices.yml", false);
        }
        priceConfig = YamlConfiguration.loadConfiguration(file);
    }

    public double getPrice(ItemStack item) {
        // Busca o preço pelo nome do material (ex: DIAMOND) no prices.yml
        return priceConfig.getDouble(item.getType().name(), -1.0);
    }
}
