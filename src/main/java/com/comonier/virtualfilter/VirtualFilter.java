package com.comonier.virtualfilter;

import com.comonier.virtualfilter.commands.FilterCommands;
import com.comonier.virtualfilter.database.DatabaseManager;
import com.comonier.virtualfilter.listeners.*;
import com.comonier.virtualfilter.manager.*;
import com.comonier.virtualfilter.integration.ShopGUIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualFilter extends JavaPlugin {
    private static VirtualFilter instance;
    private static Economy econ = null; // Variável para armazenar a economia do Vault
    private DatabaseManager dbManager;
    private ConfirmationManager confirmationManager;
    private InfinityManager infinityManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Inicializa arquivos de configuração
        saveDefaultConfig();
        
        // 2. Inicializa o sistema de preços interno (prices.yml)
        ShopGUIHook.loadPrices();

        // 3. Inicializa a conexão com o Vault (Dinheiro)
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 4. Inicializa Gerenciadores e Banco de Dados
        this.dbManager = new DatabaseManager();
        this.dbManager.setupDatabase();
        this.confirmationManager = new ConfirmationManager();
        this.infinityManager = new InfinityManager();

        // 5. Registra Comandos
        FilterCommands cmd = new FilterCommands();
        getCommand("abf").setExecutor(cmd);
        getCommand("isf").setExecutor(cmd);
        getCommand("asf").setExecutor(cmd);
        getCommand("addabf").setExecutor(cmd);
        getCommand("addisf").setExecutor(cmd);
        getCommand("addasf").setExecutor(cmd);
        getCommand("tfc").setExecutor(cmd);
        getCommand("vfreload").setExecutor(cmd);

        // 6. Registra Eventos
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new FilterProcessor(), this);

        getLogger().info("VirtualFilter has been enabled with Vault integration!");
    }

    /**
     * Configura a integração com o Vault Economy.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Recarrega configurações e preços sem reiniciar o servidor.
     */
    public void reloadPlugin() {
        reloadConfig();
        ShopGUIHook.loadPrices();
        getLogger().info("Configuration and prices reloaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VirtualFilter has been disabled.");
    }

    // --- Getters Públicos ---
    
    public static VirtualFilter getInstance() { 
        return instance; 
    }

    public static Economy getEconomy() {
        return econ;
    }
    
    public DatabaseManager getDbManager() { 
        return dbManager; 
    }
    
    public ConfirmationManager getConfirmationManager() { 
        return confirmationManager; 
    }
    
    public InfinityManager getInfinityManager() { 
        return infinityManager; 
    }
}
