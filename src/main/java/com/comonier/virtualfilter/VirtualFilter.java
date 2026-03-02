package com.comonier.virtualfilter;

import com.comonier.virtualfilter.commands.*;
import com.comonier.virtualfilter.database.DatabaseCore;
import com.comonier.virtualfilter.database.FilterRepository;
import com.comonier.virtualfilter.database.SettingsRepository;
import com.comonier.virtualfilter.listeners.*;
import com.comonier.virtualfilter.manager.processor.*;
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
    
    // Divisao Database (1 para 3)
    private DatabaseCore dbCore;
    private SettingsRepository settingsRepo;
    private FilterRepository filterRepo;
    
    // Divisao Processadores (1 para 4)
    private ReportManager reportManager;
    private FilterEngine filterEngine;

    private FileConfiguration messages;

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Configuracoes e Precos
        saveDefaultConfig();
        loadMessages();
        ShopGUIHook.loadPrices();

        // 2. Integracao com Economia (Vault)
        if (null == getServer().getPluginManager().getPlugin("Vault")) {
            getLogger().severe("Vault not found! AutoSell feature will not pay players.");
        } else {
            setupEconomy();
        }

        // 3. Inicializacao do Banco de Dados (Modular)
        this.dbCore = new DatabaseCore();
        this.dbCore.setupDatabase();
        this.settingsRepo = new SettingsRepository(dbCore.getConnection());
        this.filterRepo = new FilterRepository(dbCore.getConnection());
        
        // 4. Inicializacao dos Motores de Processamento (Modular)
        this.reportManager = new ReportManager();
        this.filterEngine = new FilterEngine(this.reportManager);

        // 5. Registro de Comandos (Divisao Modular)
        
        // Comandos de Admin
        AdminCommands adminCmd = new AdminCommands();
        getCommand("vfreload").setExecutor(adminCmd);

        // Comandos de Automacao e Logs (Independencia de Logs v1.7)
        AutomationCommands autoCmd = new AutomationCommands();
        getCommand("al").setExecutor(autoCmd); 
        getCommand("afh").setExecutor(autoCmd);
        getCommand("lo").setExecutor(autoCmd);
        getCommand("la").setExecutor(autoCmd);
        getCommand("vfat").setExecutor(autoCmd);

        // Comandos de Menu
        MenuCommands menuCmd = new MenuCommands();
        getCommand("abf").setExecutor(menuCmd);
        getCommand("isf").setExecutor(menuCmd);
        getCommand("asf").setExecutor(menuCmd);

        // Comandos de Modificacao (Add / Rem)
        AddFilterCommand addCmd = new AddFilterCommand();
        getCommand("addabf").setExecutor(addCmd);
        getCommand("addisf").setExecutor(addCmd);
        getCommand("addasf").setExecutor(addCmd);

        RemFilterCommand remCmd = new RemFilterCommand();
        getCommand("remabf").setExecutor(remCmd);
        getCommand("remisf").setExecutor(remCmd);
        getCommand("remasf").setExecutor(remCmd);

        // Comandos de Saque e Ajuda
        StorageCommands storageCmd = new StorageCommands();
        getCommand("isg").setExecutor(storageCmd);

        HelpCommand helpCmd = new HelpCommand();
        getCommand("vfhelp").setExecutor(helpCmd);

        // --- REGISTRO AUTOMATICO DE TAB COMPLETE ---
        TabCompleteListener tc = new TabCompleteListener();
        String[] allCmds = {
            "abf", "isf", "asf", "addabf", "addisf", "addasf", 
            "remabf", "remisf", "remasf", "isg", "al", "afh", 
            "lo", "la", "vfhelp", "vfreload"
        };
        for (String s : allCmds) {
            if (null != getCommand(s)) {
                getCommand(s).setTabCompleter(tc);
            }
        }

        // 6. Registro de Listeners
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new AutoFillListener(), this);
        getServer().getPluginManager().registerEvents(new BlockLootListener(this.filterEngine, this.reportManager), this);
        getServer().getPluginManager().registerEvents(new EntityLootListener(this.filterEngine), this);

        getLogger().info("VirtualFilter v1.7 (Modular) - TabComplete & Logs Enabled.");
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (false == file.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMsg(String playerLang, String path) {
        // Logica inversa para verificar idioma padrao
        String lang = (null == playerLang) ? getConfig().getString("language", "en") : playerLang;
        String msg = messages.getString(lang + "." + path);
        if (null == msg) return "§cMessage not found: " + path;
        return msg.replace("&", "§");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (null == rsp) return false;
        econ = rsp.getProvider();
        return null != econ;
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();
        ShopGUIHook.loadPrices();
        getLogger().info("Configurations reloaded.");
    }

    @Override
    public void onDisable() {
        if (null != dbCore) dbCore.closeConnection();
        getLogger().info("VirtualFilter disabled.");
    }

    // Getters Modulares
    public static VirtualFilter getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }
    public SettingsRepository getSettingsRepo() { return settingsRepo; }
    public FilterRepository getFilterRepo() { return filterRepo; }
    public FilterEngine getFilterEngine() { return filterEngine; }
    public ReportManager getReportManager() { return reportManager; }
    public DatabaseCore getDbCore() { return dbCore; }
}
