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

        player.sendMessage("§8-=-=-=-=-=-=-=-=-=-");
        player.sendMessage("§0- §8[§a§lVF §3Commands§8]");
        player.sendMessage("§0- §8[§9OpenGui§8]§f§l:§8[§7/§aisf§8][§7/§aisfe§8][§7/§easf§8][§7/§babf§8]");
        player.sendMessage("§0- §8[§aJava§2Create§8]§f§l:§8[§cShift§8]§f§l+§r§8][§cLClick§8][§7on§6inv§8][§7on§6item§8]");
        player.sendMessage("§0--§8>§7to §8[§3create§8][§3add§8][§3merge§8]");
        player.sendMessage("§0- §8[§dBedrock§5Create§8]§f§l:§8[§7/§fadd§aisf§8][§7/§fadd§aisfe§8][§7/§fadd§easf§8][§7/§fadd§babf§8]");
        player.sendMessage("§0- §8[§aJava§2WitD§8]§f§l:§8[§cShift§8]§f§l+§r§8][§cLClick§8][§7on§6filter§8][§7on§6icon§8]");
        player.sendMessage("§0--§8>§7to §8[§3withdraw§8][§3all§8][§3possible§8]");
        player.sendMessage("§0- §8[§dBedrock§5WitD§8]§f§l:§8[§7/§fget§aisf§8][§7/§fget§aisfe§8][§7slot§aid§8][§6amount§8][§6pack§8][§6all§8]");
        player.sendMessage("§0- §8[§aJava§2Rem§8]§f§l:§8[§cShift§8]§f§l+§r§8][§cRClick§8][§7to§0:§cremove§8]");
        player.sendMessage("§0- §8[§dBedrock§5Rem§8]§f§l:§8[§7/§frem§aisf§8][§7/§frem§aisfe§8][§7/§frem§easf§8][§7/§frem§babf§8][§7slot§aid§8]");
        player.sendMessage("§0- §8[§7Filters Slots must be §bempty §7to be removed§8]");
        player.sendMessage("§0- §8[§9AutoLoot§8]§f§l:§8[§7/§dal§8]§0:§8[§9AutoFillHand§8]§0:§8[§7/§dafh§8]");
        player.sendMessage("§0- §8[§9LogOwn§8]§f§l:§8[§7/§dlo§8]§0:§8[§9LogAll§8]§0:§8[§7/§dla§8]");
        player.sendMessage("§0- §8[§9Safedrop§8]§f§l:§8[§7/§dsd§8][§7Toggles §610§fs §7drop protection§8]");
        player.sendMessage("§0- §8[§9Notification§8]§f§l:§8[§7/§dvfat§8][§7§7Toggles notifications in Action Bar§8]");
        player.sendMessage("§0- §8[§9Language§8]§f§l:§8[§7/§dvflang§8][§7Switches§8][§dEn§8][§dPt§8]");
        player.sendMessage("§8-=-=-=-=-=-=-=-=-=-");

        return true;
    }
}
