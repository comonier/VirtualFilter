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
        
        // Lógica inversa: Se o arquivo não existe (false == exists)
        if (false == file.exists()) {
            VirtualFilter.getInstance().saveResource("prices.yml", false);
        }
        
        prices = YamlConfiguration.loadConfiguration(file);
    }

    public static double getItemPrice(Player player, ItemStack item) {
        // SEGURANÇA: Se o item for nulo, o preço é zero (evita erro de compilação/execução)
        if (null == item) {
            return 0.0;
        }

        String material = item.getType().name();
        
        // Se o item NÃO estiver configurado no prices.yml
        if (false == prices.contains(material)) {
            // Regra v1.6: Itens lixo valem 0.1 para não competir com minérios
            return 0.1;
        }
        
        return prices.getDouble(material);
    }

    // NOVO MÉTODO: Necessário para o FilterProcessor validar a hierarquia ASF > ISF
    public static boolean hasPrice(String materialName) {
        if (null == prices) {
            return false;
        }
        return prices.contains(materialName);
    }
}
