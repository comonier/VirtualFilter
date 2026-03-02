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
        
        // Logica inversa: Se o arquivo nao existe (false == exists) salva o recurso
        if (false == file.exists()) {
            VirtualFilter.getInstance().saveResource("prices.yml", false);
        }
        
        prices = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Retorna o preco do item definido no prices.yml
     */
    public static double getItemPrice(Player player, ItemStack item) {
        // SEGURANCA: Se o item for nulo, o preco e zero
        if (null == item) {
            return 0.0;
        }

        String material = item.getType().name();
        
        // Se o item NAO estiver configurado no prices.yml (logica inversa)
        if (false == prices.contains(material)) {
            // Regra v1.6: Itens nao listados valem 0.1 (fallback configurado)
            return 0.1;
        }
        
        double price = prices.getDouble(material);
        
        // Garante que precos negativos nao quebrem a economia (logica inversa)
        if (0.0 > price) {
            return 0.0;
        }
        
        return price;
    }

    /**
     * Valida se um material especifico tem preco definido (usado no FilterEngine)
     */
    public static boolean hasPrice(String materialName) {
        if (null == prices) {
            return false;
        }
        return prices.contains(materialName);
    }
}
