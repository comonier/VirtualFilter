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
import java.util.UUID;

public class FilterCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Declaração explícita para o compilador não se perder
        if (false == (sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar os comandos do VirtualFilter.");
            return true;
        }

        Player player = (Player) sender;
        String cmd = label.toLowerCase();
        UUID uuid = player.getUniqueId();
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(uuid);

        // --- ADMIN: RELOAD ---
        if (cmd.equals("vfreload")) {
            if (player.hasPermission("virtualfilter.admin")) {
                VirtualFilter.getInstance().reloadPlugin();
                player.sendMessage("§a[VirtualFilter] Configurações e preços recarregados!");
            } else {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "no_permission"));
            }
            return true;
        }

        // --- MENUS (ABF, ISF, ASF) ---
        if (cmd.equals("abf") || cmd.equals("isf") || cmd.equals("asf")) {
            FilterMenu.open(player, cmd);
            return true;
        }

        // --- ADICIONAR FILTRO (Slot automático ou por ID) ---
        if (cmd.equals("addasf") || cmd.equals("addisf") || cmd.equals("addabf")) {
            String type = cmd.replace("add", "");
            ItemStack hand = player.getInventory().getItemInMainHand();
            
            if (null == hand || hand.getType() == Material.AIR) {
                player.sendMessage("§cSegure um item na mão!");
                return true;
            }

            if (2 > hand.getType().getMaxStackSize() || (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName())) {
                player.sendMessage("§cItem inválido para filtros.");
                return true;
            }

            int allowed = getMaxSlots(player, type);
            int targetSlot = -1;

            if (args.length > 0) {
                try {
                    int requestedSlot = Integer.parseInt(args[0]) - 1;
                    if (0 > requestedSlot || requestedSlot >= allowed) {
                        player.sendMessage("§cID de slot inválido ou bloqueado.");
                        return true;
                    }
                    targetSlot = requestedSlot;
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUse um número válido.");
                    return true;
                }
            } else {
                for (int i = 0; allowed > i; i++) {
                    if (null == VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(uuid, type, i)) {
                        targetSlot = i;
                        break;
                    }
                }
            }

            if (-1 != targetSlot) {
                String currentMat = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(uuid, type, targetSlot);
                if (null != currentMat) {
                    player.sendMessage("§cSlot " + (targetSlot + 1) + " ocupado.");
                    return true;
                }

                saveItem(player, type, targetSlot, hand.getType().name(), 0);
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "added")
                        .replace("%item%", hand.getType().name())
                        .replace("%type%", type.toUpperCase()) + " §7(ID: " + (targetSlot + 1) + ")");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
            } else {
                player.sendMessage("§cSem slots livres!");
            }
            return true;
        }

        // --- REMOVER FILTRO ---
        if (cmd.equals("remasf") || cmd.equals("remisf") || cmd.equals("remabf")) {
            String type = cmd.replace("rem", "");
            boolean removed = false;
            int targetSlot = -1;

            if (args.length > 0) {
                try {
                    targetSlot = Integer.parseInt(args[0]) - 1;
                    if (0 > targetSlot || targetSlot >= 54) {
                        player.sendMessage("§cID inválido.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUse um número.");
                    return true;
                }
            } else {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (null != hand && hand.getType() != Material.AIR) {
                    String mat = hand.getType().name();
                    for (int i = 0; 54 > i; i++) {
                        if (mat.equalsIgnoreCase(VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(uuid, type, i))) {
                            targetSlot = i;
                            break;
                        }
                    }
                }
            }

            if (-1 != targetSlot) {
                if (type.equals("isf")) {
                    String matAtSlot = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(uuid, type, targetSlot);
                    if (null != matAtSlot && VirtualFilter.getInstance().getDbManager().getISFAmount(uuid, matAtSlot) > 0) {
                        player.sendMessage("§c§lERRO: §fSaque o estoque primeiro!");
                        return true;
                    }
                }
                removed = VirtualFilter.getInstance().getDbManager().removeAndShift(uuid, type, targetSlot);
            }

            if (removed) {
                player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "removed").replace("%type%", type.toUpperCase()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return true;
        }

        // --- SAQUE (ISG) ---
        if (cmd.equals("isg") || cmd.equals("sacar")) {
            if (1 > args.length) {
                player.sendMessage("§cUse: /isg [ID] [Quantidade]");
                return true;
            }

            try {
                int slotId = Integer.parseInt(args[0]) - 1;
                String mat = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(uuid, "isf", slotId);
                if (null == mat) return true;

                long available = VirtualFilter.getInstance().getDbManager().getISFAmount(uuid, mat);
                int amount = 64;

                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("all")) {
                        amount = (int) Math.min(available, 2304L);
                    } else {
                        amount = Integer.parseInt(args[1]);
                    }
                }

                int taken = VirtualFilter.getInstance().getDbManager().withdrawFromISF(uuid, mat, amount);
                if (taken > 0) {
                    Material m = Material.getMaterial(mat);
                    if (null != m) {
                        HashMap<Integer, ItemStack> left = player.getInventory().addItem(new ItemStack(m, taken));
                        if (false == left.isEmpty()) {
                            for (ItemStack s : left.values()) {
                                VirtualFilter.getInstance().getDbManager().addAmount(uuid, mat, (long) s.getAmount());
                            }
                        }
                        player.sendMessage(VirtualFilter.getInstance().getMsg(lang, "withdraw")
                                .replace("%amount%", String.valueOf(taken)).replace("%item%", mat));
                    }
                }
            } catch (Exception e) {}
            return true;
        }

        // --- TOGGLES ---
        if (cmd.equals("al")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoLoot(uuid);
            boolean st = VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "autoloot_on" : "autoloot_off"));
            return true;
        }
        if (cmd.equals("afh")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoFill(uuid);
            boolean st = VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "autofill_on" : "autofill_off"));
            return true;
        }
        if (cmd.equals("vfcb")) {
            VirtualFilter.getInstance().getDbManager().toggleChestDebug(uuid);
            boolean st = VirtualFilter.getInstance().getDbManager().isChestDebugEnabled(uuid);
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, st ? "chest_debug_on" : "chest_debug_off"));
            return true;
        }

        return true;
    }

    private int getMaxSlots(Player p, String type) {
        if (p.isOp() || p.hasPermission("virtualfilter.admin") || p.hasPermission("*")) return 54;
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            String s = perm.getPermission().toLowerCase();
            if (s.startsWith("virtualfilter." + type + ".")) {
                try {
                    int v = Integer.parseInt(s.substring(s.lastIndexOf(".") + 1));
                    if (v > max) max = v;
                } catch (Exception ignored) {}
            }
        }
        if (max >= 54) return 54;
        return max;
    }

    private void saveItem(Player p, String type, int slot, String mat, long amount) {
        String query = "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(query)) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot);
            ps.setString(4, mat.toUpperCase());
            ps.setLong(5, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
