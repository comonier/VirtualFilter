package com.comonier.virtualfilter.menu;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class FilterMenu {

    public static void open(Player player, String type) {
        String title = switch (type.toLowerCase()) {
            case "abf" -> "§8§lAutoBlockFilter";
            case "isf" -> "§8§lInfinityStackFilter";
            case "asf" -> "§8§lAutoSellFilter";
            default -> "§8§lFilter";
        };

        Inventory inv = Bukkit.createInventory(null, 54, title);
        int allowedSlots = getMaxSlots(player, type.toLowerCase());

        for (int i = 0; i < 54; i++) {
            int displaySlot = i + 1;
            if (i >= allowedSlots) {
                ItemStack locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = locked.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cSlot " + displaySlot + " [LOCKED]");
                    inv.setItem(i, locked);
                }
            } else {
                String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
                if (matName != null) {
                    Material mat = Material.getMaterial(matName);
                    if (mat != null) {
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§aSlot " + displaySlot);
                            List<String> lore = new ArrayList<>();
                            if (type.equalsIgnoreCase("isf")) {
                                long amount = VirtualFilter.getInstance().getInfinityManager().getAmount(player.getUniqueId(), matName);
                                lore.add("§7Stored: §f" + amount);
                            }
                            lore.add("§8ID: " + mat.name());
                            lore.add("§7Right-click to remove.");
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(i, item);
                    }
                }
            }
        }
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    private static int getMaxSlots(Player player, String type) {
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int value = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (value > max) max = value;
                } catch (NumberFormatException ignored) {}
            }
        }
        return Math.min(max, 54);
    }
}
