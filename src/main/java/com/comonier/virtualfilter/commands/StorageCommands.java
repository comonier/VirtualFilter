package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class StorageCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        if (1 > args.length) {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "syntax_error")
                    .replace("%cmd%", label).replace("%args%", "[slot] [all/pack/amount]"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        try {
            int slotId = Integer.parseInt(args[0]) - 1;
            String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, "isf", slotId);

            if (null == matName) { 
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "filter_not_found")); 
                return true; 
            }

            long available = VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName);
            if (0 >= available) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, "isf", slotId);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "filter_not_found"));
                return true;
            }

            int req = 64;
            if (1 < args.length) {
                String sub = args[1].toLowerCase();
                if (sub.equals("all")) {
                    req = (int) Math.min(available, 2304L);
                } else if (sub.equals("pack")) {
                    req = 64;
                } else {
                    req = Integer.parseInt(args[1]);
                }
            }

            int taken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, matName, req);
            
            if (taken > 0) {
                Material mat = Material.getMaterial(matName);
                if (null != mat) {
                    ItemStack isfItem = new ItemStack(mat, taken);
                    int remaining = fillSpecificSlots(player, isfItem);
                    int kept = taken - remaining;

                    if (remaining > 0) {
                        VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) remaining);
                        player.sendMessage("§6[VF] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }

                    if (kept > 0) {
                        String withdrawMsg = VirtualFilter.getInstance().getMsg(lang, "withdraw")
                                .replace("%amount%", String.valueOf(kept))
                                .replace("%item%", matName);
                        player.sendMessage(withdrawMsg);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    }
                }
            }

            if (0 >= VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName)) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, "isf", slotId);
            }

        } catch (NumberFormatException e) { 
            player.sendMessage("§c§lERROR: §fUse números válidos."); 
        }
        return true;
    }

    private int fillSpecificSlots(Player player, ItemStack toAdd) {
        int left = toAdd.getAmount();
        ItemStack[] contents = player.getInventory().getStorageContents();
        
        // Passada 1: Completa stacks de itens que NAO possuem Meta
        for (ItemStack invItem : contents) {
            if (invItem != null && invItem.getType() == toAdd.getType()) {
                if (invItem.hasItemMeta()) {
                    if (invItem.getItemMeta().hasDisplayName() || invItem.getItemMeta().hasCustomModelData()) {
                        continue;
                    }
                }
                
                int canAdd = invItem.getMaxStackSize() - invItem.getAmount();
                if (canAdd > 0) {
                    int taking = Math.min(left, canAdd);
                    invItem.setAmount(invItem.getAmount() + taking);
                    left -= taking;
                }
            }
            if (0 >= left) break;
        }
        
        // Passada 2: Preenche slots vazios
        if (left > 0) {
            for (int i = 0; contents.length > i; i++) {
                if (contents[i] == null || contents[i].getType() == Material.AIR) {
                    int taking = Math.min(left, toAdd.getMaxStackSize());
                    ItemStack newItem = toAdd.clone();
                    newItem.setAmount(taking);
                    player.getInventory().setItem(i, newItem);
                    left -= taking;
                }
                if (0 >= left) break;
            }
        }
        return left;
    }
}
