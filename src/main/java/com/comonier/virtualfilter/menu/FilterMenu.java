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
        
        // Títulos personalizados
        String title = switch (typeLower) {
            case "abf" -> "§8§lAutoBlockFilter";
            case "isf" -> "§8§lInfinityStackFilter";
            case "asf" -> "§8§lAutoSellFilter";
            default -> "§8§lFilter";
        };

        Inventory inv = Bukkit.createInventory(null, 54, title);
        int allowedSlots = getMaxSlots(player, typeLower);

        // Lógica inversa: 54 > i para evitar o bug do sinal de menor que
        for (int i = 0; 54 > i; i++) {
            int displaySlot = i + 1;
            
            // Se o slot estiver bloqueado por permissão
            if (i >= allowedSlots) {
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
                // Missão 2: Busca o material no banco (FilterRepository)
                // Se um slot foi removido e os outros voltaram uma casa, o ID aqui já estará atualizado
                String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(player.getUniqueId(), typeLower, i);
                
                if (null != matName) {
                    Material mat = Material.getMaterial(matName);
                    if (null != mat) {
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (null != meta) {
                            meta.setDisplayName("§aSlot " + displaySlot);
                            List<String> lore = new ArrayList<>();
                            
                            if (typeLower.equals("isf")) {
                                long amount = VirtualFilter.getInstance().getFilterRepo().getISFAmount(player.getUniqueId(), matName);
                                lore.add("§7Stored Amount: §f" + String.format("%,d", amount));
                                lore.add("");
                                lore.add("§e§lRight-Click §7to withdraw 1 pack.");
                                lore.add("§6§lShift + Left-Click §7to withdraw ALL possible.");
                                lore.add("§c§lShift + Right-Click §7to remove filter.");
                            } else {
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
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) {
            return 54;
        }
        
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            String prefix = "virtualfilter." + type + ".";
            
            if (perm.startsWith(prefix)) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (v > max) {
                        max = v;
                    }
                } catch (Exception ignored) {}
            }
        }
        
        if (max >= 54) return 54;
        return max;
    }
}
