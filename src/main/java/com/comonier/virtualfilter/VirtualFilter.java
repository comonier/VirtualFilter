package com.comonier.virtualfilter;

import com.comonier.virtualfilter.commands.FilterCommands;
import com.comonier.virtualfilter.database.DatabaseManager;
import com.comonier.virtualfilter.listeners.*;
import com.comonier.virtualfilter.manager.*;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class VirtualFilter extends JavaPlugin {
    private static VirtualFilter instance;
    private static Economy econ = null;
    private DatabaseManager dbManager;
    private InfinityManager infinityManager;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Configurações e Preços
        saveDefaultConfig();
        loadMessages();
        ShopGUIHook.loadPrices();

        // 2. Integração com Economia (Vault)
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! AutoSell feature will not pay players.");
        }

        // 3. Banco de Dados e Gerenciadores
        this.dbManager = new DatabaseManager();
        this.dbManager.setupDatabase();
        this.infinityManager = new InfinityManager();

        // 4. Registro de Comandos
        FilterCommands cmd = new FilterCommands();
        getCommand("abf").setExecutor(cmd);
        getCommand("isf").setExecutor(cmd);
        getCommand("asf").setExecutor(cmd);
        getCommand("addabf").setExecutor(cmd);
        getCommand("addisf").setExecutor(cmd);
        getCommand("addasf").setExecutor(cmd);
        getCommand("vfat").setExecutor(cmd);
        getCommand("vflang").setExecutor(cmd);
        getCommand("vfhelp").setExecutor(cmd);
        getCommand("vfreload").setExecutor(cmd);
        getCommand("afh").setExecutor(cmd); // Novo comando integrado

        // 5. Registro de Listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new FilterProcessor(), this);
        getServer().getPluginManager().registerEvents(new AutoFillListener(), this); // Novo listener integrado

        getLogger().info("VirtualFilter v1.1 enabled successfully (AutoFillHand included)!");
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMsg(String playerLang, String path) {
        String lang = (playerLang == null) ? getConfig().getString("language", "en") : playerLang;
        return messages.getString(lang + "." + path, "Message not found: " + path);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();
        ShopGUIHook.loadPrices();
        getLogger().info("Configurations and prices reloaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VirtualFilter disabled.");
    }

    // --- Getters Públicos ---
    public static VirtualFilter getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }
    public DatabaseManager getDbManager() { return dbManager; }
    public InfinityManager getInfinityManager() { return infinityManager; }
}
