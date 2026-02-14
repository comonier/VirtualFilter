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
        String title = switch (typeLower) {
            case "abf" -> "§8§lAutoBlockFilter";
            case "isf" -> "§8§lInfinityStackFilter";
            case "asf" -> "§8§lAutoSellFilter";
            default -> "§8§lFilter";
        };

        Inventory inv = Bukkit.createInventory(null, 54, title);
        int allowedSlots = getMaxSlots(player, typeLower);

        for (int i = 0; i < 54; i++) {
            int displaySlot = i + 1;
            
            if (i >= allowedSlots) {
                ItemStack locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = locked.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cSlot " + displaySlot + " §l- LOCKED");
                    List<String> lore = new ArrayList<>();
                    lore.add("§7This slot is restricted.");
                    lore.add("§eRank up §7to unlock this space.");
                    meta.setLore(lore);
                    locked.setItemMeta(meta);
                    inv.setItem(i, locked);
                }
            } else {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), typeLower, i);
                if (matName != null) {
                    Material mat = Material.getMaterial(matName);
                    if (mat != null) {
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§aSlot " + displaySlot);
                            List<String> lore = new ArrayList<>();
                            
                            if (typeLower.equals("isf")) {
                                long amount = VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName);
                                lore.add("§7Stored Amount: §f" + String.format("%,d", amount));
                                lore.add("");
                                lore.add("§e§lRight-Click §7to withdraw 1 pack.");
                                lore.add("§6§lShift + Left-Click §7to withdraw ALL possible.");
                                lore.add("§c§lShift + Right-Click §7to remove filter.");
                                lore.add("§4§lWARNING: §cRemoving this filter will");
                                lore.add("§cpermanently delete the stored stock!");
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
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        return Math.min(max, 54);
    }
}
