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

        // 1. Admin: Reload
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

        // 2. Help Menu
        if (cmd.equals("vfhelp")) {
            sendHelp(player);
            return true;
        }

        // 3. Language Selection
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
                player.sendMessage("§cInvalid language! Options: en, pt");
            }
            return true;
        }

        // 4. Toggle Confirmation (TFC)
        if (cmd.equals("vftc")) {
            VirtualFilter.getInstance().getDbManager().toggleConfirmation(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isConfirmEnabled(player.getUniqueId());
            String status = newState ? "§aENABLED" : "§cDISABLED";
            player.sendMessage("§6[VirtualFilter] §7Deletion confirmation is now " + status);
            return true;
        }

        // 5. Toggle Action Bar (AT)
        if (cmd.equals("vfat")) {
            VirtualFilter.getInstance().getDbManager().toggleActionBar(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isActionBarEnabled(player.getUniqueId());
            String status = newState ? "§aENABLED" : "§cDISABLED";
            player.sendMessage("§6[VirtualFilter] §7Action Bar notifications are now " + status);
            return true;
        }

        // 6. Filter Menus
        if (cmd.equals("abf") || cmd.equals("isf") || cmd.equals("asf")) {
            FilterMenu.open(player, cmd);
            return true;
        }

        // 7. Add Items
        if (cmd.startsWith("add")) {
            String type = cmd.replace("add", "");
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR || item.getType().getMaxStackSize() <= 1) {
                player.sendMessage("§cInvalid item! Only stackable items are allowed.");
                return true;
            }
            int allowed = getMaxSlots(player, type);
            int slot = -1;
            for (int i = 0; i < allowed; i++) {
                if (VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i) == null) {
                    slot = i; break;
                }
            }
            if (slot == -1) { 
                player.sendMessage("§cNo slots available! Upgrade your rank."); 
                return true; 
            }
            saveItem(player, type, slot, item.getType().name());
            player.sendMessage("§aItem added to " + type.toUpperCase() + " slot " + (slot + 1));
            return true;
        }
        return false;
    }

    private void sendHelp(Player p) {
        String lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(p.getUniqueId());
        if (lang.equalsIgnoreCase("pt")) {
            p.sendMessage("§6§l--- VirtualFilter Ajuda ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Abre os menus de filtro.");
            p.sendMessage("§e/add<tipo> §7- Adiciona o item da mão ao filtro.");
            p.sendMessage("§e/vfat §7- Ativa/Desativa avisos na Action Bar.");
            p.sendMessage("§e/vftc §7- Ativa/Desativa confirmação de exclusão.");
            p.sendMessage("§e/vflang <en|pt> §7- Altera seu idioma pessoal.");
            p.sendMessage("§e/vfhelp §7- Mostra este menu de ajuda.");
            p.sendMessage("§7Dica: Shift+Click Esquerdo no inventário adiciona itens rápido.");
        } else {
            p.sendMessage("§6§l--- VirtualFilter Help ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Open filter menus.");
            p.sendMessage("§e/add<type> §7- Add held item to filter.");
            p.sendMessage("§e/vfat §7- Toggle Action Bar notifications.");
            p.sendMessage("§e/vftc §7- Toggle deletion confirmation.");
            p.sendMessage("§e/vflang <en|pt> §7- Change your personal language.");
            p.sendMessage("§e/vfhelp §7- Show this help menu.");
            p.sendMessage("§7Tip: Shift+Left Click in inventory for Quick-Add.");
        }
    }

    private int getMaxSlots(Player player, String type) {
        int max = VirtualFilter.getInstance().getConfig().getInt("default-slots." + type, 1);
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String perm = permission.getPermission();
            if (perm.startsWith("virtualfilter." + type + ".")) {
                try {
                    int value = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                    if (value > max) max = value;
                } catch (NumberFormatException ignored) {}
            }
        }
        return Math.min(max, 54);
    }

    private void saveItem(Player p, String type, int slot, String mat) {
        try (PreparedStatement ps = VirtualFilter.getInstance().getDbManager().getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, 0)")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.setString(4, mat);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
