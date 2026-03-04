package com.comonier.virtualfilter.commands;
import com.comonier.virtualfilter.VirtualFilter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
public class StorageCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (args.length < 1) {
            player.sendMessage("§c§lSYNTAX ERROR: §f/isg [Slot ID] [all/pack/amount]");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }
        try {
            int slotId = Integer.parseInt(args[0]) - 1;
            String matName = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, "isf", slotId);
            if (matName == null) { player.sendMessage("§c§lERROR: §fSlot vazio."); return true; }
            long available = VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName);
            if (available <= 0) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, "isf", slotId);
                player.sendMessage("§c§lERROR: §fEstoque vazio. Filtro removido.");
                return true;
            }
            int req = 64;
            if (args.length > 1) {
                String sub = args[1].toLowerCase();
                if (sub.equals("all")) req = (int) Math.min(available, 2304L);
                else if (sub.equals("pack")) req = 64;
                else req = Integer.parseInt(args[1]);
            }
            int taken = VirtualFilter.getInstance().getFilterRepo().withdrawFromISF(uuid, matName, req);
            if (taken > 0) {
                Material mat = Material.getMaterial(matName);
                if (mat != null) {
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(mat, taken));
                    int kept = taken;
                    if (!left.isEmpty()) {
                        int rem = 0; for (ItemStack s : left.values()) rem += s.getAmount();
                        VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, (long) rem);
                        kept = taken - rem;
                        player.sendMessage("§6[VF] §c§lFULL INVENTORY!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    if (kept > 0) {
                        player.sendMessage("§6[VF] §aWithdrawn §f" + kept + "x §e" + matName);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                    }
                }
            }
            if (VirtualFilter.getInstance().getFilterRepo().getISFAmount(uuid, matName) <= 0) {
                VirtualFilter.getInstance().getFilterRepo().removeAndShift(uuid, "isf", slotId);
            }
        } catch (NumberFormatException e) { player.sendMessage("§c§lERROR: §fUse números válidos."); }
        return true;
    }
}
