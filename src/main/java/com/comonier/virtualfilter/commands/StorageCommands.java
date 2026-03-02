package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class StorageCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        if (1 > args.length) {
            player.sendMessage("§c§lSYNTAX ERROR: §f/isg [Slot ID] [all/pack/amount]");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        try {
            int slotId = Integer.parseInt(args[0]) - 1;
            String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, "isf", slotId);
            
            if (null == matName) {
                player.sendMessage("§c§lERROR: §fSlot vazio.");
                return true;
            }

            long available = VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName);
            if (0 >= available) {
                player.sendMessage("§c§lERROR: §fEstoque vazio.");
                return true;
            }

            int amountToRequest = 64;
            if (args.length > 1) {
                String sub = args[1].toLowerCase();
                if (sub.equals("all")) {
                    amountToRequest = (int) Math.min(available, 2304L);
                } else if (sub.equals("pack")) {
                    amountToRequest = 64;
                } else {
                    amountToRequest = Integer.parseInt(args[1]);
                }
            }

            int taken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, matName, amountToRequest);
            
            if (taken > 0) {
                Material mat = Material.getMaterial(matName);
                if (null != mat) {
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(mat, taken));
                    
                    if (false == left.isEmpty()) {
                        for (ItemStack overflow : left.values()) {
                            VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) overflow.getAmount());
                        }
                        // TAG SEM NEGRITO (§6[VF])
                        player.sendMessage("§6[VF] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    
                    int kept = taken;
                    if (false == left.isEmpty()) {
                        kept = taken - left.get(0).getAmount();
                    }

                    if (kept > 0) {
                        // TAG SEM NEGRITO E ID REAL (§e + matName)
                        player.sendMessage("§6[VF] §aWithdrawn §f" + kept + "x §e" + matName);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    }
                }
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c§lERROR: §fUse números válidos.");
        }
        return true;
    }
}
