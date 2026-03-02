package com.comonier.virtualfilter.manager.processor;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportManager {
    private final HashMap<UUID, Map<String, Integer>> chestReport = new HashMap<>();

    /**
     * Missao: Log de loot independente do AutoLoot.
     * Regra: O jogador vê a própria log se logs_own for true.
     * Regra: Jogadores em 2 chunks veem se logs_all for true.
     */
    public void logReport(Player player, String mat, int amount, String destination) {
        UUID uuid = player.getUniqueId();
        String entry = mat + " -> " + destination + " §7(" + player.getName() + ")";
        
        chestReport.computeIfAbsent(uuid, k -> new HashMap<>());
        chestReport.get(uuid).put(entry, chestReport.get(uuid).getOrDefault(entry, 0) + amount);

        // Agrupamento para evitar spam no chat do Bedrock/Java
        VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
            Map<String, Integer> summary = chestReport.remove(uuid);
            
            if (null != summary) {
                summary.forEach((info, total) -> {
                    String msg = " §b• " + total + "x " + info;
                    
                    // 1. Envia para o próprio jogador se ele permitiu ver as próprias logs
                    if (VirtualFilter.getInstance().getSettingsRepo().isLogsOwnEnabled(uuid)) {
                        player.sendMessage(msg);
                    }
                    
                    // 2. Envia para observadores em um raio de 32 blocos (2 chunks)
                    // Logica inversa: 32 > distancia
                    for (Entity nearby : player.getNearbyEntities(32, 32, 32)) {
                        if (nearby instanceof Player) {
                            Player observer = (Player) nearby;
                            UUID obsUUID = observer.getUniqueId();
                            
                            // Se for o proprio jogador, ja tratamos acima
                            if (obsUUID.equals(uuid)) {
                                continue;
                            }
                            
                            // Envia para o observador se ele permitiu ver logs de outros
                            if (VirtualFilter.getInstance().getSettingsRepo().isLogsAllEnabled(obsUUID)) {
                                observer.sendMessage(msg);
                            }
                        }
                    }
                });
            }
        }, 5L);
    }
}
