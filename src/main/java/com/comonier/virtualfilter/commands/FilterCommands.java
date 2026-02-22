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
            sender.sendMessage("§a[VirtualFilter] Configurations and Prices reloaded!");
            return true;
        }

        if (!(sender instanceof Player player)) return true;

        // Tenta pegar o idioma, se falhar usa "en" (Evita o Crash do seu log)
        String lang = "en";
        try {
            lang = VirtualFilter.getInstance().getDbManager().getPlayerLanguage(player.getUniqueId());
        } catch (Exception ignored) {}
        if (lang == null) lang = "en";

        // --- Comando AutoLoot ---
        if (cmd.equals("al")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoLoot(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isAutoLootEnabled(player.getUniqueId());
            String msgKey = newState ? "autoloot_on" : "autoloot_off";
            player.sendMessage(VirtualFilter.getInstance().getMsg(lang, msgKey));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            return true;
        }

        // Agora aceita vfhelp, vf e vfilter
        if (cmd.equals("vfhelp") || cmd.equals("vf") || cmd.equals("vfilter")) {
            sendHelp(player, lang);
            return true;
        }

        // --- Comando AutoFillHand ---
        if (cmd.equals("afh")) {
            VirtualFilter.getInstance().getDbManager().toggleAutoFill(player.getUniqueId());
            boolean newState = VirtualFilter.getInstance().getDbManager().isAutoFillEnabled(player.getUniqueId());
            String status = newState ? "§aENABLED" : "§cDISABLED";
            player.sendMessage("§6[VirtualFilter] §7AutoFillHand is now " + status);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            return true;
        }

        // --- Comando Idioma ---
        if (cmd.equals("vflang")) {
            if (args.length == 0) {
                player.sendMessage("§eUsage: /vflang <en|pt>");
                return true;
            }
            String newLang = args[0].toLowerCase();
            if (newLang.equals("en") || newLang.equals("pt")) {
                VirtualFilter.getInstance().getDbManager().setPlayerLanguage(player.getUniqueId(), newLang);
                player.sendMessage(newLang.equals("en") ? "§aLanguage set to English!" : "§aIdioma definido para Português!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else {
                player.sendMessage("§cInvalid language!");
            }
            return true;
        }

        // --- Comandos de Menu (ABF, ISF, ASF) ---
        if (cmd.equals("abf") || cmd.equals("isf") || cmd.equals("asf")) {
            FilterMenu.open(player, cmd);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            return true;
        }

        // --- Comandos de Adição (/addabf, /addisf, /addasf) ---
        if (cmd.startsWith("add")) {
            String type = cmd.replace("add", "");
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (item.getType() == Material.AIR || item.getType().getMaxStackSize() <= 1) {
                player.sendMessage("§cInvalid item! Only stackable items allowed.");
                return true;
            }

            Material mat = item.getType();
            int allowed = getMaxSlots(player, type);
            int targetSlot = -1;

            for (int i = 0; i < allowed; i++) {
                String m = VirtualFilter.getInstance().getDbManager().getMaterialAtSlot(player.getUniqueId(), type, i);
                if (m == null) {
                    targetSlot = i;
                    break;
                } else if (m.equalsIgnoreCase(mat.name())) {
                    player.sendMessage("§cFilter already exists for " + mat.name());
                    return true;
                }
            }
            
            if (targetSlot == -1) {
                player.sendMessage("§cNo slots available!");
                return true;
            }

            saveItem(player, type, targetSlot, mat.name(), 0);
            player.sendMessage("§aAdded to " + type.toUpperCase() + " slot " + (targetSlot + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            return true;
        }
        return false;
    }

    private void sendHelp(Player p, String lang) {
        if (lang.equalsIgnoreCase("pt")) {
            p.sendMessage("§6§l--- VirtualFilter Ajuda ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Abre os menus de filtro.");
            p.sendMessage("§e/add<tipo> §7- Adiciona item da mão ao filtro.");
            p.sendMessage("§e/al §7- Ativa/Desativa o AutoLoot.");
            p.sendMessage("§e/afh §7- Ativa/Desativa o AutoFillHand.");
            p.sendMessage("§e/vflang <en|pt> §7- Altera seu idioma.");
            p.sendMessage("§e/vfhelp §7- Mostra este menu.");
        } else {
            p.sendMessage("§6§l--- VirtualFilter Help ---");
            p.sendMessage("§e/abf, /isf, /asf §7- Open filter menus.");
            p.sendMessage("§e/add<type> §7- Add held item to filter.");
            p.sendMessage("§e/al §7- Toggle AutoLoot.");
            p.sendMessage("§e/afh §7- Toggle AutoFillHand.");
            p.sendMessage("§e/vflang <en|pt> §7- Change your language.");
            p.sendMessage("§e/vfhelp §7- Show this help menu.");
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
