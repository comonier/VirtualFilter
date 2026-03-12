package com.comonier.virtualfilter;

import com.comonier.virtualfilter.commands.*;
import com.comonier.virtualfilter.database.DatabaseCore;
import com.comonier.virtualfilter.database.FilterRepository;
import com.comonier.virtualfilter.database.FilterEditRepository;
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
import java.util.HashMap;
import java.util.Map;

public class VirtualFilter extends JavaPlugin {
    private static VirtualFilter instance;
    private static Economy econ = null;
    private DatabaseCore dbCore;
    private SettingsRepository settingsRepo;
    private FilterRepository filterRepo;
    private FilterEditRepository filterEditRepo;
    private ReportManager reportManager;
    private FilterEngine filterEngine;
    private final Map<String, FileConfiguration> langFiles = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadMessages();
        ShopGUIHook.loadPrices();

        if (null == getServer().getPluginManager().getPlugin("Vault")) {
            getLogger().severe("Vault not found!");
        } else {
            setupEconomy();
        }

        this.dbCore = new DatabaseCore();
        this.dbCore.setupDatabase();

        this.settingsRepo = new SettingsRepository(dbCore.getConnection());
        this.filterRepo = new FilterRepository(dbCore.getConnection());
        this.filterEditRepo = new FilterEditRepository(dbCore.getEditConnection());

        this.reportManager = new ReportManager();
        this.filterEngine = new FilterEngine(this.reportManager);

        getCommand("vfreload").setExecutor(new AdminCommands());
        AutomationCommands autoCmd = new AutomationCommands();
        getCommand("al").setExecutor(autoCmd);
        getCommand("afh").setExecutor(autoCmd);
        getCommand("lo").setExecutor(autoCmd);
        getCommand("la").setExecutor(autoCmd);
        getCommand("vfat").setExecutor(autoCmd);
        getCommand("sd").setExecutor(autoCmd);

        MenuCommands menuCmd = new MenuCommands();
        getCommand("abf").setExecutor(menuCmd);
        getCommand("isf").setExecutor(menuCmd);
        getCommand("asf").setExecutor(menuCmd);
        getCommand("isfe").setExecutor(menuCmd);

        AddFilterCommand addCmd = new AddFilterCommand();
        getCommand("addabf").setExecutor(addCmd);
        getCommand("addisf").setExecutor(addCmd);
        getCommand("addasf").setExecutor(addCmd);
        getCommand("addisfe").setExecutor(new AddFilterEditCommand());

        RemFilterCommand remCmd = new RemFilterCommand();
        getCommand("remabf").setExecutor(remCmd);
        getCommand("remisf").setExecutor(remCmd);
        getCommand("remasf").setExecutor(remCmd);
        getCommand("remisfe").setExecutor(new RemFilterEditCommand());

        getCommand("getisf").setExecutor(new StorageCommands());
        getCommand("getisfe").setExecutor(new GetFilterEditCommand());
        getCommand("vfhelp").setExecutor(new HelpCommand());

        TabCompleteListener tc = new TabCompleteListener();
        String[] allCmds = {"abf","isf","asf","isfe","addabf","addisf","addasf","addisfe","remabf","remisf","remasf","remisfe","getisf","getisfe","al","afh","lo","la","sd","vfhelp","vfreload"};
        for (String s : allCmds) { 
            if (null != getCommand(s)) getCommand(s).setTabCompleter(tc); 
        }

        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractionEditListener(), this);
        getServer().getPluginManager().registerEvents(new AutoFillListener(), this);
        getServer().getPluginManager().registerEvents(new BlockLootListener(this.filterEngine, this.reportManager), this);
        getServer().getPluginManager().registerEvents(new EntityLootListener(this.filterEngine), this);

        new Metrics(this, 29969);
        getLogger().info("VirtualFilter v1.8.0 Enabled.");
    }

    private void loadMessages() {
        langFiles.clear();
        File folder = getDataFolder();
        File[] files = folder.listFiles();
        if (null != files) {
            int i = 0;
            while (files.length > i) {
                File f = files[i];
                String name = f.getName();
                if (name.startsWith("messages_") && name.endsWith(".yml")) {
                    String langTag = name.replace("messages_", "").replace(".yml", "");
                    langFiles.put(langTag, YamlConfiguration.loadConfiguration(f));
                }
                i++;
            }
        }
        if (langFiles.isEmpty()) {
            saveResource("messages_en.yml", false);
            saveResource("messages_pt.yml", false);
            loadMessages();
        }
    }

    public String getMsg(String playerLang, String path) {
        String lang = (null == playerLang) ? getConfig().getString("language", "en") : playerLang;
        FileConfiguration config = langFiles.getOrDefault(lang, langFiles.get("en"));
        String msg = config.getString(path);
        if (null == msg && false == lang.equals("en")) msg = langFiles.get("en").getString(path);
        return (null == msg) ? "§cMsg not found: " + path : msg.replace("&", "§");
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
        getLogger().info("Reloaded.");
    }

    @Override 
    public void onDisable() {
        if (null != dbCore) dbCore.closeConnection();
        instance = null;
    }

    public static VirtualFilter getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }
    public SettingsRepository getSettingsRepo() { return settingsRepo; }
    public FilterRepository getFilterRepo() { return filterRepo; }
    public FilterEditRepository getFilterEditRepo() { return filterEditRepo; }
    public FilterEngine getFilterEngine() { return filterEngine; }
    public ReportManager getReportManager() { return reportManager; }
    public DatabaseCore getDbCore() { return dbCore; }
}
