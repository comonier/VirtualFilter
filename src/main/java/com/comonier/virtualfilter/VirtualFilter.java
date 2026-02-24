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
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found! AutoSell feature will not pay players.");
        } else {
            setupEconomy();
        }

        // 3. Banco de Dados e Gerenciadores
        this.dbManager = new DatabaseManager();
        this.dbManager.setupDatabase();
        this.infinityManager = new InfinityManager();

        // 4. Registro de Comandos (Centralizado no FilterCommands)
        FilterCommands cmd = new FilterCommands();
        
        // Menus e Configurações
        getCommand("abf").setExecutor(cmd);
        getCommand("isf").setExecutor(cmd);
        getCommand("asf").setExecutor(cmd);
        getCommand("vfat").setExecutor(cmd);
        getCommand("vflang").setExecutor(cmd);
        getCommand("vfhelp").setExecutor(cmd);
        getCommand("vfreload").setExecutor(cmd);
        
        // Funções de Automação
        getCommand("al").setExecutor(cmd); 
        getCommand("afh").setExecutor(cmd);

        // Adição e Remoção (v1.4+)
        getCommand("addabf").setExecutor(cmd);
        getCommand("addisf").setExecutor(cmd);
        getCommand("addasf").setExecutor(cmd);
        getCommand("remabf").setExecutor(cmd);
        getCommand("remisf").setExecutor(cmd);
        getCommand("remasf").setExecutor(cmd);
        getCommand("isg").setExecutor(cmd);

        // NOVO v1.5: Comando de Debug de Baús (vfcb)
        getCommand("vfcb").setExecutor(cmd);

        // 5. Registro de Listeners (Eventos)
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new FilterProcessor(), this);
        getServer().getPluginManager().registerEvents(new AutoFillListener(), this);

        getLogger().info("VirtualFilter v1.5 enabled successfully! Chest Guard system active.");
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        // LOGICA INVERSA: Se o arquivo nao existir
        if (false == file.exists()) saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMsg(String playerLang, String path) {
        // Fallback seguro: se playerLang for nulo, usa o da config
        String lang = (null == playerLang) ? getConfig().getString("language", "en") : playerLang;
        String msg = messages.getString(lang + "." + path);
        
        if (null == msg) return "§cMessage not found: " + path;
        return msg.replace("&", "§");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        // LOGICA INVERSA: Se o provedor for nulo
        if (null == rsp) return false;
        econ = rsp.getProvider();
        return null != econ;
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();
        ShopGUIHook.loadPrices();
        getLogger().info("Configurations and prices reloaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VirtualFilter v1.5 disabled.");
    }

    public static VirtualFilter getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }
    public DatabaseManager getDbManager() { return dbManager; }
    public InfinityManager getInfinityManager() { return infinityManager; }
}
