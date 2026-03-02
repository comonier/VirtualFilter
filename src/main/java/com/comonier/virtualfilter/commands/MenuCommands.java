package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Logica inversa: Se nao for jogador, aborta
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        // Abre o menu correspondente (abf, isf ou asf)
        FilterMenu.open(player, cmd);
        
        return true;
    }
}
