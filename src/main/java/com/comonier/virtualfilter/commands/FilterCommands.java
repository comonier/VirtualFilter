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
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class FilterCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = label.toLowerCase();
        if (cmd.equals("vfreload")) {
            if (!sender.hasPermission("virtualfilter.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            VirtualFilter.getInstance().reloadPlugin();
            sender.sendMessage("§a[VirtualFilter] Configurations and Prices reloaded!");
            return true;
        }

        if (!(sender instanceof Player player)) return true;

        String lang = "en";
        try { lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId()); } catch (Exception ignored) {}
        if (lang == null) lang = "en";

        if (cmd.equals("vfhelp") || cmd.equals("vf") || cmd.equals("vfilter")) {
            sendHelp(player, lang);
            return true;
        }

        if (cmd.equals("al")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoLoot(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId());
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, newState ? "autoloot_on" : "autoloot_off"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            return true;
        }

        if (cmd.equals("afh")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoFill(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId());
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, newState ? "autofill_on" : "autofill_off"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            return true;
        }

        if (cmd.equals("abf") || cmd.equals("isf") || cmd.equals("asf")) {
            FilterMenu.open(player, cmd);
            return true;
        }

        if (cmd.startsWith("add")) {
            String type = cmd.replace("add", "");
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) return true;
            Material mat = item.getType();
            int allowed = getMaxSlots(player, type);
            int targetSlot = -1;

            for (int i = 0; allowed > i; i++) {
                String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
                if (m == null) { targetSlot = i; break; }
                if (m.equalsIgnoreCase(mat.name())) return true;
            }

            if (targetSlot != -1) {
                saveItem(player, type, targetSlot, mat.name(), 0);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "added").replace("%item%", mat.name()).replace("%type%", type.toUpperCase()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
            }
            return true;
        }

        if (cmd.startsWith("rem")) {
            String type = cmd.replace("rem", "");
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) return true;

            if (VirtualFilter.getInstance().getDbManager().removeFilterMaterial(player.getUniqueId(), type, item.getType().name())) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "removed").replace("%item%", item.getType().name()).replace("%type%", type.toUpperCase()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return true;
        }

        // --- SACAR DO ISF POR SLOT ID (/isg <slot> [quantia|all]) ---
        if (cmd.equals("isg") || cmd.equals("sacar")) {
            if (2 > args.length && !cmd.equals("sacar")) { // Exige ao menos o slot
                player.sendMessage("§eUsage: /isg <slot_id> [amount|all]");
                return true;
            }

            int slotId;
            try {
                slotId = Integer.parseInt(args[0]) - 1; // Ajusta de 1-54 para 0-53
            } catch (Exception e) {
                player.sendMessage("§cSlot ID must be a number!");
                return true;
            }

            String matName = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), "isf", slotId);
            if (matName == null) {
                player.sendMessage("§cNo filter found in Slot " + (slotId + 1));
                return true;
            }

            long available = VirtualFilter.getInstance().getDbManager().getISFAmount(player.getUniqueId(), matName);
            if (0 >= available) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "no_item_isf").replace("%item%", matName));
                return true;
            }

            int toWithdraw = 64;
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("all")) toWithdraw = (int) Math.min(available, 2304);
                else try { toWithdraw = Integer.parseInt(args[1]); } catch (Exception e) { toWithdraw = 64; }
            }

            int taken = VirtualFilter.getInstance().getDbManager().withdrawFromISF(player.getUniqueId(), matName, toWithdraw);
            if (taken > 0) {
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(Material.getMaterial(matName), taken));
                if (!left.isEmpty()) {
                    for (ItemStack s : left.values()) VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), matName, s.getAmount());
                    player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "inv_full"));
                }
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "withdraw").replace("%amount%", String.valueOf(taken)).replace("%item%", matName));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            }
            return true;
        }
        return false;
    }

    private void sendHelp(Player p, String lang) {
        if (lang.equalsIgnoreCase("pt")) {
            p.sendMessage("§6§l--- VirtualFilter Ajuda ---");
            p.sendMessage("§e/add<tipo> §7- Adiciona item da mão.");
            p.sendMessage("§e/rem<tipo> §7- Remove item da mão.");
            p.sendMessage("§e/isg <slot> <quantia|all> §7- Saca do ISF.");
            p.sendMessage("§e/al, /afh §7- Alterna Loot/Fill.");
            p.sendMessage("§e/vfhelp §7- Este menu.");
        } else {
            p.sendMessage("§6§l--- VirtualFilter Help ---");
            p.sendMessage("§e/add<type> §7- Add held item.");
            p.sendMessage("§e/rem<type> §7- Remove held item.");
            p.sendMessage("§e/isg <slot> <amount|all> §7- Withdraw from ISF.");
            p.sendMessage("§e/al, /afh §7- Toggle Loot/Fill.");
            p.sendMessage("§e/vfhelp §7- This help menu.");
        }
    }

    private int getMaxSlots(Player player, String type) {
        if (player.isOp() || player.hasPermission("virtualfilter.admin")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int v = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        return Math.min(max, 54);
    }

    private void saveItem(Player p, String type, int slot, String mat, long amount) {
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat);
            ps.setLong(5, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
