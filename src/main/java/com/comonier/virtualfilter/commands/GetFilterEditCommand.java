package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.UUID;

public class GetFilterEditCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        if (1 > args.length) {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "syntax_error")
                    .replace("%cmd%", label).replace("%args%", "[Slot ID] [all/pack/amount]"));
            return true;
        }

        try {
            int slotId = Integer.parseInt(args[0]) - 1;
            String[] data = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, "isfe", slotId);

            if (null == data) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "filter_not_found"));
                return true;
            }

            String matName = data[0];
            String customName = data[1];

            long available = VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(uuid, matName, customName);
            if (0 >= available) {
                VirtualFilter.getInstance().getFilterEditRepo().removeAndShift(uuid, "isfe", slotId);
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

            int taken = VirtualFilter.getInstance().getFilterEditRepo().withdrawFromISFE(uuid, matName, customName, req);

            if (taken > 0) {
                Material mat = Material.getMaterial(matName);
                if (null != mat) {
                    ItemStack itemToGive = new ItemStack(mat, taken);
                    ItemMeta meta = itemToGive.getItemMeta();
                    if (null != meta) {
                        meta.setDisplayName(customName);
                        itemToGive.setItemMeta(meta);
                    }

                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(itemToGive);
                    if (false == left.isEmpty()) {
                        int rem = 0;
                        for (ItemStack s : left.values()) rem += s.getAmount();
                        VirtualFilter.getInstance().getFilterEditRepo().addAmount(uuid, matName, customName, (long) rem);
                        player.sendMessage("§6[VFE] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    
                    String withdrawMsg = VirtualFilter.getInstance().getMsg(lang, "withdraw")
                            .replace("%amount%", String.valueOf(taken))
                            .replace("%item%", customName);
                    player.sendMessage("§6[VFE] " + withdrawMsg);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                }
            }

            if (0 >= VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(uuid, matName, customName)) {
                VirtualFilter.getInstance().getFilterEditRepo().removeAndShift(uuid, "isfe", slotId);
            }

        } catch (NumberFormatException e) {
            player.sendMessage("§c§lERROR: §fUse números válidos.");
        }
        return true;
    }
}
