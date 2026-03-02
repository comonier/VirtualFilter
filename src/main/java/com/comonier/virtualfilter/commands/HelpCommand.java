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
        // Logica inversa: Se nao for jogador, aborta
        if (false == (sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        // Cabecalho da ajuda
        player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "help_header"));
        
        // Linhas de comando traduzidas
        sendHelp(player, lang, "abf/isf/asf", "Open filter menus");
        sendHelp(player, lang, "add<type> [slot]", "Add item to filter");
        sendHelp(player, lang, "rem<type> [slot]", "Remove item filter");
        sendHelp(player, lang, "isg [slot] [all]", "Withdraw ISF items (Bedrock)");
        sendHelp(player, lang, "al / afh", "Toggle AutoLoot / AutoFill");
        sendHelp(player, lang, "lo / la", "Toggle Personal/Nearby Logs");

        return true;
    }

    private void sendHelp(Player p, String lang, String cmd, String desc) {
        p.sendMessage(VirtualFilter.getInstance().getMsg(lang, "help_line")
                .replace("%cmd%", cmd).replace("%desc%", desc));
    }
}
