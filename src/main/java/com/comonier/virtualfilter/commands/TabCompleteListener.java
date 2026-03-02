package com.comonier.virtualfilter.commands;

import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TabCompleteListener implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (false == (sender instanceof Player)) return null;
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String cmd = command.getName().toLowerCase();

        List<String> completions = new ArrayList<>();

        // 1. Sugestoes de Slots para REMOCAO e SAQUE
        if (cmd.startsWith("rem") || cmd.equals("isg")) {
            if (1 == args.length) {
                String type = cmd.replace("rem", "").replace("isg", "isf");
                for (int i = 0; 54 > i; i++) {
                    if (null != VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i)) {
                        completions.add(String.valueOf(i + 1));
                    }
                }
            }
            if (cmd.equals("isg") && 2 == args.length) {
                completions.addAll(Arrays.asList("all", "pack"));
            }
        }

        // 2. Sugestoes de Slots Livres para ADICAO (Bedrock)
        if (cmd.startsWith("add")) {
            if (1 == args.length) {
                String type = cmd.replace("add", "");
                int allowed = 54; // Simplificado para tab
                for (int i = 0; allowed > i; i++) {
                    if (null == VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i)) {
                        completions.add(String.valueOf(i + 1));
                        if (completions.size() > 5) break; // Nao poluir o chat
                    }
                }
            }
        }

        // 3. Sugestoes para o comando principal /vf
        if (cmd.equals("vfhelp") || cmd.equals("vf")) {
            if (1 == args.length) {
                completions.addAll(Arrays.asList("help", "reload", "lang", "at"));
            }
        }

        // Filtro final por caracteres digitados
        List<String> result = new ArrayList<>();
        String lastArg = args[args.length - 1].toLowerCase();
        for (String s : completions) {
            if (s.toLowerCase().startsWith(lastArg)) {
                result.add(s);
            }
        }
        return result;
    }
}
