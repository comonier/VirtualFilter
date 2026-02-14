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
            sender.sendMessage("§a[VirtualFilter] Configurations and Prices reloaded successfully!");
            return true;
        }

        if (!(sender instanceof Player player)) return true;

        if (cmd.equals("vfhelp")) {
            sendHelp(player);
            return true;
        }

        if (cmd.equals("vflang")) {
            if (args.length == 0) {
                player.sendMessage("§eUsage: /vflang <en|pt>");
                return true;
            }
            String lang = args[0].toLowerCase();
            if (lang.equals("en") || lang.equals("pt")) {
                VirtualFilter.getInstance().getDbManager().setPlayerLanguage(player.getUniqueId(), lang);
                player.sendMessage(lang.equals("en") ? "§aLanguage set to English!" : "§aIdioma definido para Português!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else {
                player.sendMessage("§cInvalid language!");
            }
            return true;
        }

        if (cmd.equals("vfat")) {
            VirtualFilter.getInstance().getDbManager().toggleActionBar(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId());
            String status = newState ? "§aENABLED" : "§cDISABLED";
            player.sendMessage("§6[VirtualFilter] §7Action Bar notifications are now " + status);
            return true;
        }

        if (cmd.equals("abf") || cmd.equals("isf") || cmd.equals("asf")) {
            FilterMenu.open(player, cmd);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            return true;
        }

        if (cmd.startsWith("add")) {
            String type = cmd.replace("add", "");
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (item.getType() == Material.AIR || item.getType().getMaxStackSize() <= 1) {
                player.sendMessage("§cInvalid item! Non-stackable items are not allowed.");
                return true;
            }

            Material mat = item.getType();
            
            // --- NOVA LÓGICA DE AUTO-MESCLAGEM VIA COMANDO ---
            int existingSlot = -1;
            for (int i = 0; i < 54; i++) {
                String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
                if (m != null && m.equalsIgnoreCase(mat.name())) {
                    existingSlot = i;
                    break;
                }
            }

            if (existingSlot != -1) {
                if (type.equals("isf")) {
                    long toAdd = 0;
                    for (ItemStack invItem : player.getInventory().getContents()) {
                        if (invItem != null && invItem.getType() == mat) {
                            toAdd += invItem.getAmount();
                            invItem.setAmount(0);
                        }
                    }
                    VirtualFilter.getInstance().getDbManager().addAmount(player.getUniqueId(), mat.name(), (int) toAdd);
                    player.sendMessage("§aItems merged into your existing " + mat.name() + " filter.");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                } else {
                    player.sendMessage("§cYou already have a filter for " + mat.name() + "!");
                }
                return true;
            }

            // --- LÓGICA DE CRIAÇÃO (SE NÃO EXISTIR) ---
            int allowed = getMaxSlots(player, type);
            int targetSlot = -1;
            for (int i = 0; i < allowed; i++) {
                if (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i) == null) {
                    targetSlot = i; break;
                }
            }
            
            if (targetSlot == -1) {
                player.sendMessage("§cNo slots available! Upgrade your rank for more space.");
                return true;
            }

            long initialAmount = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == mat) {
                        initialAmount += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }

            saveItem(player, type, targetSlot, mat.name(), initialAmount);
            player.sendMessage("§aAdded to " + type.toUpperCase() + " slot " + (targetSlot + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            return true;
        }
        return false;
    }

    private void sendHelp(Player p) {
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(p.getUniqueId());
        if (lang.equalsIgnoreCase("pt")) {
            p.sendMessage("§6§l--- VirtualFilter Ajuda ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Abre os menus de filtro.");
            p.sendMessage("§e/add<tipo> §7- Adiciona/Mescla item da mão ao filtro.");
            p.sendMessage("§e/vfat §7- Ativa/Desativa avisos na Action Bar.");
            p.sendMessage("§e/vflang <en|pt> §7- Altera seu idioma pessoal.");
            p.sendMessage("§e/vfhelp §7- Mostra este menu de ajuda.");
        } else {
            p.sendMessage("§6§l--- VirtualFilter Help ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Open filter menus.");
            p.sendMessage("§e/add<type> §7- Add/Merge held item to filter.");
            p.sendMessage("§e/vfat §7- Toggle Action Bar notifications.");
            p.sendMessage("§e/vflang <en|pt> §7- Change your personal language.");
            p.sendMessage("§e/vfhelp §7- Show this help menu.");
        }
    }

    private int getMaxSlots(Player player, String type) {
        if (player.isOp() || player.hasPermission("virtualfilter.admin") || player.hasPermission("*")) return 54;
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
