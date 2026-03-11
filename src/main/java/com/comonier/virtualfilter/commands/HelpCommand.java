package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        player.sendMessage("§6§l--- VirtualFilter Help ---");
        player.sendMessage("§9/asf §7| §9/isf §7| §9/isfe §7| §9/abf §7- §eOpen GUIs");
        player.sendMessage("§9/add§c<f> §7| §9/rem§c<f> §7[slot] §7- §eManage Filters");
        player.sendMessage("§bFilters§7: §7<§casf§7|§cisf§7|§cisfe§7|§cabf§7> §7(Item in hand)");
        player.sendMessage("§9/getisf §7| §9/getisfe §b<slot> <amount|all|pack> §7- §eWithdraw");
        player.sendMessage("§9/al §7| §9/afh §7| §9/vfat §7| §9/sd §7- §eToggles");
        player.sendMessage("§9/lo §7| §9/la §7- §eLoot Logs §7(Own/Nearby)");
        player.sendMessage("§9/vflang §7| §9/vfreload §7- §eSettings");
        player.sendMessage("§aJava§7: §bshift§7+§9left click §7in inventory to add filter.");

        return true;
    }
}
