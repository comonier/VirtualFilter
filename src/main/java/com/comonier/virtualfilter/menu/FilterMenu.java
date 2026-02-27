package com.comonier.virtualfilter.menu;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class FilterMenu {

    public static void open(Player player, String type) {
        String typeLower = type.toLowerCase();
        // Switch para títulos personalizados conforme o menu aberto
        String title = switch (typeLower) {
            case "abf" -> "§8§lAutoBlockFilter";
            case "isf" -> "§8§lInfinityStackFilter";
            case "asf" -> "§8§lAutoSellFilter";
            default -> "§8§lFilter";
        };

        // Inventário padrão de 54 slots (6 linhas)
        Inventory inv = Bukkit.createInventory(null, 54, title);
        int allowedSlots = getMaxSlots(player, typeLower);

        // Lógica inversa: 54 > i para evitar o bug do sinal de menor que
        for (int i = 0; 54 > i; i++) {
            int displaySlot = i + 1;
            
            if (i >= allowedSlots) {
                // Representação visual de slot bloqueado (Rank insuficiente)
                ItemStack locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = locked.getItemMeta();
                if (null != meta) {
                    meta.setDisplayName("§cSlot " + displaySlot + " §l- LOCKED");
                    List<String> lore = new ArrayList<>();
                    lore.add("§7This slot is restricted.");
                    lore.add("§eRank up §7to unlock this space.");
                    meta.setLore(lore);
                    locked.setItemMeta(meta);
                    inv.setItem(i, locked);
                }
            } else {
                // Busca no banco de dados se há um material registrado para este slot
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeLower, i);
                if (null != matName) {
                    Material mat = Material.getMaterial(matName);
                    if (null != mat) {
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (null != meta) {
                            meta.setDisplayName("§aSlot " + displaySlot);
                            List<String> lore = new ArrayList<>();
                            
                            // Lógica específica para o menu de Estoque Infinito
                            if (typeLower.equals("isf")) {
                                long amount = VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName);
                                // Formatação numérica com separador (ex: 1.000.000)
                                lore.add("§7Stored Amount: §f" + String.format("%,d", amount));
                                lore.add("");
                                lore.add("§e§lRight-Click §7to withdraw 1 pack.");
                                lore.add("§6§lShift + Left-Click §7to withdraw ALL possible.");
                                lore.add("§c§lShift + Right-Click §7to remove filter.");
                                lore.add("§4§lWARNING: §cRemoving this filter will");
                                lore.add("§cpermanently delete the stored stock!");
                            } else {
                                // Lore simplificada para AutoBlock e AutoSell
                                lore.add("§8Material: " + mat.name());
                                lore.add("§c§lShift + Right-Click §7to remove filter.");
                            }
                            
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(i, item);
                    }
                }
            }
        }
        player.openInventory(inv);
    }

    private static int getMaxSlots(Player player, String type) {
        // Bypass total para operadores ou permissão curinga
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) {
            return 54;
        }
        
        // Pega o valor base definido no arquivo config.yml
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        
        // Varredura de permissões dinâmicas (ex: virtualfilter.isf.27)
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            String prefix = "virtualfilter." + type + ".";
            
            if (perm.startsWith(prefix)) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    // Mantém sempre o maior valor encontrado entre as permissões
                    if (v > max) {
                        max = v;
                    }
                } catch (Exception ignored) {}
            }
        }
        
        // Garante que o retorno nunca ultrapasse o limite físico do menu (54 slots)
        if (max >= 54) {
            return 54;
        }
        return max;
    }
}
