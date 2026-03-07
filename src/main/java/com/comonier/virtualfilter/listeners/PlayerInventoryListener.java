package com.comonier.virtualfilter.listeners;
import com.comonier.virtualfilter.VirtualFilter;
import com.comonier.virtualfilter.menu.FilterMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.UUID;
public class PlayerInventoryListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != player.getInventory()) return;
        String title = event.getView().getTitle();
        if (!title.contains("Filter")) return;
        if (event.getClick() == ClickType.SHIFT_LEFT) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (clickedItem.getType().getMaxStackSize() < 2 || (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName())) {
                player.sendMessage("§cItem inválido para filtros.");
                return;
            }
            event.setCancelled(true);
            String typeCode = title.contains("AutoBlock") ? "abf" : (title.contains("InfinityStack") ? "isf" : "asf");
            handleProcess(player, typeCode, clickedItem);
            VirtualFilter.getInstance().getServer().getScheduler().runTaskLater(VirtualFilter.getInstance(), () -> {
                player.updateInventory();
                FilterMenu.open(player, typeCode);
            }, 2L);
        }
    }
    private void handleProcess(Player player, String type, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Material matType = item.getType();
        String matName = matType.name();
        if (VirtualFilter.getInstance().getFilterRepo().hasFilter(uuid, type, matName)) {
            if (type.equals("isf")) {
                long totalAdded = 0;
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        totalAdded += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
                if (totalAdded > 0) {
                    VirtualFilter.getInstance().getFilterRepo().addAmount(uuid, matName, totalAdded);
                    player.sendMessage("§6[VF] §f" + totalAdded + "x §e" + matName + " §7merged!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                }
            }
            return;
        }
        int allowed = getMaxSlots(player, type);
        int targetSlot = -1;
        for (int i = 0; i < allowed; i++) {
            String existing = VirtualFilter.getInstance().getFilterRepo().getMaterialAtSlot(uuid, type, i);
            if (existing == null || existing.isEmpty() || existing.equalsIgnoreCase("AIR")) {
                targetSlot = i; break;
            }
        }
        if (targetSlot != -1) {
            long total = 0;
            if (type.equals("isf")) {
                for (ItemStack invItem : player.getInventory().getStorageContents()) {
                    if (invItem != null && invItem.getType() == matType && invItem.getEnchantments().isEmpty()) {
                        total += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
            saveToDB(uuid, type, targetSlot, matName, total);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
            if (total > 0 && type.equals("isf")) player.sendMessage("§6[VF] §f" + total + "x §e" + matName + " §7stored!");
        }
    }
    private int getMaxSlots(Player p, String type) {
        if (p.isOp() || p.hasPermission("virtualfilter.admin")) return 54;
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
        return (max >= 54) ? 54 : max;
    }
    private void saveToDB(UUID uuid, String type, int slot, String mat, long amount) {
        String query = "INSERT OR REPLACE INTO player_filters (uuid, filter_type, slot_id, material, amount) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = VirtualFilter.getInstance().getDbCore().getConnection().prepareStatement(query)) {
            ps.setString(1, uuid.toString()); ps.setString(2, type.toLowerCase());
            ps.setInt(3, slot); ps.setString(4, mat.toUpperCase());
            ps.setLong(5, amount); ps.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }
}
