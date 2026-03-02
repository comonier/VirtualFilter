package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class AutomationCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Logica inversa: Se nao for jogador, aborta
        if (false == (sender instanceof Player)) {
            return true;
        }
        
        Player player = (Player) sender;
        String cmd = label.toLowerCase();
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);

        // --- AutoLoot (AL) ---
        if (cmd.equals("al")) {
            VirtualFilter.getInstance().getSettingsRepo().toggleAutoLoot(uuid);
            boolean st = VirtualFilter.getInstance().getSettingsRepo().isAutoLootEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "autoloot_on" : "autoloot_off"));
            return true;
        }

        // --- AutoFillHand (AFH) ---
        if (cmd.equals("afh")) {
            VirtualFilter.getInstance().getSettingsRepo().toggleAutoFill(uuid);
            boolean st = VirtualFilter.getInstance().getSettingsRepo().isAutoFillEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "autofill_on" : "autofill_off"));
            return true;
        }

        // --- Logs Proprias (lo / vflogsown) ---
        if (cmd.equals("lo") || cmd.equals("vflogsown")) {
            VirtualFilter.getInstance().getSettingsRepo().toggleLogsOwn(uuid);
            boolean st = VirtualFilter.getInstance().getSettingsRepo().isLogsOwnEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "logs_own_on" : "logs_own_off"));
            return true;
        }

        // --- Logs de Terceiros (la / vflogsall) ---
        if (cmd.equals("la") || cmd.equals("vflogsall")) {
            VirtualFilter.getInstance().getSettingsRepo().toggleLogsAll(uuid);
            boolean st = VirtualFilter.getInstance().getSettingsRepo().isLogsAllEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "logs_all_on" : "logs_all_off"));
            return true;
        }

        // --- Action Bar (vfat) ---
        if (cmd.equals("vfat")) {
            VirtualFilter.getInstance().getSettingsRepo().toggleActionBar(uuid);
            boolean st = VirtualFilter.getInstance().getSettingsRepo().isActionBarEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "actionbar_on" : "actionbar_off"));
            return true;
        }

        return true;
    }
}
