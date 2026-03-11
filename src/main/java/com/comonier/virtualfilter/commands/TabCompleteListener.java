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

        // 1. Sugestões de Slots para REMOÇÃO e SAQUE (Vanilla e Editados)
        if (cmd.startsWith("rem") || cmd.equals("getisf") || cmd.equals("getisfe")) {
            if (1 == args.length) {
                String type = cmd.replace("rem", "");
                
                // Lógica para ISFE (Itens Editados)
                if (type.equals("isfe") || cmd.equals("getisfe")) {
                    for (int i = 0; 54 > i; i++) {
                        if (null != VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, "isfe", i)) {
                            completions.add(String.valueOf(i + 1));
                        }
                    }
                } else {
                    // Lógica para Vanilla (ISF, ASF, ABF)
                    String vType = type.equals("getisf") ? "isf" : type;
                    for (int i = 0; 54 > i; i++) {
                        if (null != VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, vType, i)) {
                            completions.add(String.valueOf(i + 1));
                        }
                    }
                }
            }
            if ((cmd.equals("getisf") || cmd.equals("getisfe")) && 2 == args.length) {
                completions.addAll(Arrays.asList("all", "pack"));
            }
        }

        // 2. Sugestões de Slots Livres para ADIÇÃO (Bedrock)
        if (cmd.startsWith("add")) {
            if (1 == args.length) {
                String type = cmd.replace("add", "");
                for (int i = 0; 54 > i; i++) {
                    boolean isOccupied = (type.equals("isfe")) 
                        ? null != VirtualFilter.getInstance().getFilterEditRepo().getItemDataAtSlot(uuid, "isfe", i)
                        : null != VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i);
                    
                    if (false == isOccupied) {
                        completions.add(String.valueOf(i + 1));
                        if (completions.size() > 5) break;
                    }
                }
            }
        }

        // 3. Sugestões para o comando principal /vf
        if (cmd.equals("vfhelp") || cmd.equals("vf")) {
            if (1 == args.length) {
                completions.addAll(Arrays.asList("help", "reload", "lang", "at", "sd"));
            }
        }

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
