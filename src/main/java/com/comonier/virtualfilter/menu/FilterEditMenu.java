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

public class FilterEditMenu {

    public static void open(Player player) {
        String title = "§8§lInfinityStackFilterEdited";
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        int allowedSlots = getMaxSlots(player, "isfe");

        for (int i = 0; 54 > i; i++) {
            int displaySlot = i + 1;
            
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
                String[] data = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(player.getUniqueId(), "isfe", i);
                
                if (null != data) {
                    Material mat = Material.getMaterial(data[0]);
                    String customName = data[1];
                    
                    if (null != mat) {
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (null != meta) {
                            meta.setDisplayName(customName);
                            List<String> lore = new ArrayList<>();
                            
                            long amount = VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(player.getUniqueId(), data[0], customName);
                            lore.add("§7Stored Amount: §f" + String.format("%,d", amount));
                            lore.add("§8Slot: " + displaySlot);
                            lore.add("");
                            lore.add("§e§lRight-Click §7to withdraw 1 pack.");
                            lore.add("§6§lShift + Left-Click §7to withdraw ALL possible.");
                            lore.add("§c§lShift + Right-Click §7to remove filter.");
                            
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
        
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots.isf", 1);
        
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
