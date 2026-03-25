package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterEditMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class RemFilterEditCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
        String type = "isfe";
        int targetSlot = -1;

        if (args.length > 0) {
            try {
                targetSlot = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "syntax_error")
                        .replace("%cmd%", label).replace("%args%", "[slot]"));
                return true;
            }
        } else {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (null == hand || hand.getType() == Material.AIR || false == hand.hasItemMeta() || false == hand.getItemMeta().hasDisplayName()) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "syntax_error")
                        .replace("%cmd%", label).replace("%args%", "[slot]"));
                return true;
            }
            String customName = hand.getItemMeta().getDisplayName();
            for (int i = 0; 54 > i; i++) {
                String[] data = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, type, i);
                if (null != data && customName.equals(data[1])) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (-1 != targetSlot) {
            String[] data = VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, type, targetSlot);
            if (null == data) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "filter_not_found"));
                return true;
            }
            
            // Segurança: Nao permite remover se ainda houver estoque (logica inversa)
            long amount = VirtualFilter.getInstance().getFilterEditRepo().getISFEAmount(uuid, data[0], data[1]);
            if (0 < amount) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "delete_blocked_isf"));
                return true;
            }

            if (VirtualFilter.getInstance().getFilterEditRepo().removeAndShift(uuid, type, targetSlot)) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "removed")
                        .replace("%type%", "ISFE").replace("%item%", data[1]));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                
                // Sincronização segura para reabrir o menu ISFE
                VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                    player.updateInventory();
                    FilterEditMenu.open(player);
                }, 2L);
            }
        } else {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "filter_not_found"));
        }
        return true;
    }
}
