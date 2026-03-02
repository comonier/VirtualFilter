package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Lógica inversa: Se não for jogador, retorna erro
        if (false == (sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores.");
            return true;
        }

        Player player = (Player) sender;
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(player.getUniqueId());

        if (player.hasPermission("virtualfilter.admin")) {
            VirtualFilter.getInstance().reloadPlugin();
            player.sendMessage("§a[VirtualFilter] Configurações e preços recarregados!");
        } else {
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "no_permission"));
        }
        return true;
    }
}
