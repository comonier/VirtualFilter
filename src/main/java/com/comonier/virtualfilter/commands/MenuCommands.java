package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.menu.FilterMenu;
import com.comonier.virtualfilter.menu.FilterEditMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        if (cmd.equals("isfe")) {
            FilterEditMenu.open(player);
        } else {
            FilterMenu.open(player, cmd);
        }
        
        return true;
    }
}
