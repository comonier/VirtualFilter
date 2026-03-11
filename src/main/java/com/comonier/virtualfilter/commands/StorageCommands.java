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
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(mat, taken));
                    int kept = taken;

                    if (false == left.isEmpty()) {
                        int rem = 0; 
                        for (ItemStack s : left.values()) {
                            rem += s.getAmount();
                        }
                        VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) rem);
                        kept = taken - rem;
                        
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
}
