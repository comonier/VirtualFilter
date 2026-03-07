package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class ReportManager {
    private final HashMap<UUID, Map<String, Integer>> chestReport = new HashMap<>();
    private final Map<UUID, Set<String>> blockNotifyCache = new HashMap<>();

    public void logReport(Player player, String mat, int amount, String destKey) {
        // ASF não gera log de texto para não poluir o chat (usa Action Bar via FilterEngine)
        if (destKey.equals("log_dest_asf")) return;

        UUID uuid = player.getUniqueId();

        // Anti-Spam para itens bloqueados (ABF) - 5 segundos de silêncio por material
        if (destKey.equals("log_dest_abf")) {
            blockNotifyCache.putIfAbsent(uuid, new HashSet<>());
            if (blockNotifyCache.get(uuid).contains(mat)) return;
            blockNotifyCache.get(uuid).add(mat);
            
            VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                if (blockNotifyCache.containsKey(uuid)) blockNotifyCache.get(uuid).remove(mat);
            }, 100L);
        }

        String pName = player.getName();
        VirtualFilter vf = VirtualFilter.getInstance();
        chestReport.computeIfAbsent(uuid, k -> new HashMap<>());

        // Chave única para agrupar drops: Material | Destino | Nome do Jogador
        String logKey = mat + "|" + destKey + "|" + pName;
        chestReport.get(uuid).put(logKey, chestReport.get(uuid).getOrDefault(logKey, 0) + amount);

        // Agendamento de 5 ticks para processar o resumo (evita flood no chat)
        vf.getServer().getScheduler().runTaskLater(vf, () -> {
            Map<String, Integer> summary = chestReport.remove(uuid);
            if (null != summary) {
                summary.forEach((key, total) -> {
                    String[] parts = key.split("\\|");
                    if (parts.length >= 3) {
                        String material = parts[0];
                        String dKey = parts[1];
                        String playerName = parts[2];

                        // 1. Mensagem para o dono do loot (Tradução individual)
                        String pLang = vf.getSettingsRepo().getPlayerLanguage(uuid);
                        String translatedDest = vf.getMsg(pLang, dKey);
                        String finalMsg = " §b• " + total + "x " + material + " -> " + translatedDest + " §7(" + playerName + ")";
                        
                        if (vf.getSettingsRepo().isLogsOwnEnabled(uuid)) {
                            player.sendMessage(finalMsg);
                        }

                        // 2. Mensagem para observadores próximos (32m)
                        for (Entity nearby : player.getNearbyEntities(32, 32, 32)) {
                            if (nearby instanceof Player) {
                                Player obs = (Player) nearby;
                                UUID obsUUID = obs.getUniqueId();
                                if (obsUUID.equals(uuid)) continue;
                                
                                if (vf.getSettingsRepo().isLogsAllEnabled(obsUUID)) {
                                    String obsLang = vf.getSettingsRepo().getPlayerLanguage(obsUUID);
                                    String obsDest = vf.getMsg(obsLang, dKey);
                                    obs.sendMessage(" §b• " + total + "x " + material + " -> " + obsDest + " §7(" + playerName + ")");
                                }
                            }
                        }
                    }
                });
            }
        }, 5L);
    }
}
