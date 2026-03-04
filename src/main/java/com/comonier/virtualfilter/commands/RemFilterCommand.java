package com.comonier.virtualfilter.commands;
import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;
public class RemFilterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getSettingsRepo().getPlayerLanguage(uuid);
        String type = label.toLowerCase().replace("rem", "").replace("r", "");
        int targetSlot = -1;
        if (args.length > 0) {
            try { targetSlot = Integer.parseInt(args[0]) - 1; }
            catch (NumberFormatException e) { player.sendMessage("§c§lERROR: §fUse /" + label + " [Slot ID]"); return true; }
        } else {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage("§c§lSYNTAX ERROR: §f/" + label + " [Slot ID]"); return true;
            }
            String mat = hand.getType().name();
            for (int i = 0; i < 54; i++) {
                if (mat.equalsIgnoreCase(VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i))) {
                    targetSlot = i; break;
                }
            }
        }
        if (targetSlot != -1) {
            String matAtSlot = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, targetSlot);
            if (matAtSlot == null) { player.sendMessage("§c§lERROR: §fFilter not found."); return true; }
            if (type.equals("isf") && VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matAtSlot) > 0) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "delete_blocked_isf")); return true;
            }
            if (VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, type, targetSlot)) {
                String msg = VirtualFilter.getInstance().getMsg(lang, "removed")
                    .replace("%type%", type.toUpperCase())
                    .replace("%item%", matAtSlot);
                player.sendMessage(msg);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                FilterMenu.open(player, type);
            }
        } else { player.sendMessage("§c§lERROR: §fFilter not found."); }
        return true;
    }
}
