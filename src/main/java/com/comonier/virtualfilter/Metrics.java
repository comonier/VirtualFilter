package com.comonier.virtualfilter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
public class Metrics {
    private final Plugin plugin;
    private final int pluginId;
    private static final String B_STATS_VERSION = "3.0.2";
    private static final String URL = "https://bstats.org";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean enabled;
    private String serverUUID;
    private boolean logErrors;
    private boolean logSentData;
    private boolean logResponseStatusText;
    public Metrics(JavaPlugin plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (false == config.isSet("enabled")) {
            config.set("enabled", true);
            config.set("serverUuid", UUID.randomUUID().toString());
            config.set("logFailedRequests", false);
            config.set("logSentData", false);
            config.set("logResponseStatusText", false);
            try { config.save(configFile); } catch (IOException ignored) {}
        }
        this.enabled = config.getBoolean("enabled", true);
        this.serverUUID = config.getString("serverUuid");
        this.logErrors = config.getBoolean("logFailedRequests", false);
        this.logSentData = config.getBoolean("logSentData", false);
        this.logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        if (enabled) {
            boolean found = false;
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                if (service.getName().equals(Metrics.class.getName())) { found = true; break; }
            }
            Bukkit.getServicesManager().register(Metrics.class, this, plugin, org.bukkit.plugin.ServicePriority.Normal);
            if (false == found) { startSubmitting(); }
        }
    }
    private void startSubmitting() {
        scheduler.scheduleAtFixedRate(this::submitData, 2, 30, TimeUnit.MINUTES);
    }
    private void submitData() {
        final Map<String, Object> data = new HashMap<>();
        data.put("serverUUID", serverUUID);
        data.put("pluginVersion", plugin.getDescription().getVersion());
        data.put("pluginId", pluginId);
        data.put("osName", System.getProperty("os.name"));
        data.put("osArch", System.getProperty("os.arch"));
        data.put("osVersion", System.getProperty("os.version"));
        data.put("coreCount", Runtime.getRuntime().availableProcessors());
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("onlineMode", Bukkit.getServer().getOnlineMode() ? 1 : 0);
        data.put("bukkitVersion", Bukkit.getVersion());
        data.put("playerCount", Bukkit.getOnlinePlayers().size());
        new Thread(() -> {
            try { sendData(data); } catch (Exception e) { if (logErrors) plugin.getLogger().warning("Could not submit stats: " + e.getMessage()); }
        }).start();
    }
    private void sendData(Map<String, Object> data) throws Exception {
        String json = buildJson(data);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
        byte[] bytes = compress(json);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(bytes.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Metrics-Service/1");
        connection.setDoOutput(true);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) { outputStream.write(bytes); }
        int responseCode = connection.getResponseCode();
        if (logResponseStatusText) plugin.getLogger().info("Sent stats, response: " + responseCode);
    }
    private String buildJson(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (false == first) builder.append(",");
            builder.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) builder.append("\"").append(entry.getValue()).append("\"");
            else builder.append(entry.getValue());
            first = false;
        }
        return builder.append("}").toString();
    }
    private byte[] compress(String str) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
        gzip.close();
        return obj.toByteArray();
    }
}
