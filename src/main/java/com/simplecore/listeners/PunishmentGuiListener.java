package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.PunishmentGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PunishmentGuiListener implements Listener {
    private final SimpleCorePlugin plugin;
    public PunishmentGuiListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player staff)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        String title = e.getView().getTitle();
        if (!title.startsWith("Punish:") && !title.endsWith(" Duration")) return;
        e.setCancelled(true);

        if (title.startsWith("Punish:")) {
            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            var targetId = plugin.getPunishTarget(staff.getUniqueId());
            if (targetId == null) { staff.closeInventory(); return; }
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) { staff.sendMessage(Component.text("Target offline.")); staff.closeInventory(); return; }

            switch (name) {
                case "warn" -> {
                    target.sendMessage(Component.text("You have been warned."));
                    staff.sendMessage(Component.text("Warned " + target.getName()));
                    staff.closeInventory();
                }
                case "jail" -> plugin.getPunishmentGui().openDurations(staff, "Jail");
                case "mute" -> plugin.getPunishmentGui().openDurations(staff, "Mute");
                case "kick" -> { plugin.getPunishmentManager().kick(target, "Kicked by staff."); plugin.getLogWriter().logPunishment(staff.getUniqueId(), target.getUniqueId(), "KICK", 0, "Kicked by staff.", null); staff.closeInventory(); }
                case "temp ban" -> plugin.getPunishmentGui().openDurations(staff, "Temp Ban");
                case "perm ban" -> { plugin.getPunishmentManager().permBan(target, "Banned by staff."); plugin.getLogWriter().logPunishment(staff.getUniqueId(), target.getUniqueId(), "PERMBAN", 0, "Banned by staff.", null); staff.closeInventory(); }
                case "ip ban" -> { if (target.getAddress() != null) { String ip = target.getAddress().getAddress().getHostAddress(); plugin.getPunishmentManager().ipBan(ip, "IP Banned"); plugin.getLogWriter().logPunishment(staff.getUniqueId(), target.getUniqueId(), "IPBAN", 0, "IP Banned", "ip=" + ip); } staff.closeInventory(); }
                default -> {}
            }
        } else if (title.endsWith(" Duration")) {
            String prefix = title.substring(0, title.length() - " Duration".length());
            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            String dname = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            int hours = Integer.parseInt(dname.split(" ")[0]);
            long millis = hours * 3600L * 1000L;
            var targetId = plugin.getPunishTarget(staff.getUniqueId());
            if (targetId == null) { staff.closeInventory(); return; }
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) { staff.sendMessage(Component.text("Target offline.")); staff.closeInventory(); return; }

            switch (prefix) {
                case "Jail" -> { plugin.getPunishmentManager().jail(target, millis); staff.sendMessage(Component.text("Jailed " + target.getName() + " for " + hours + "h.")); }
                case "Mute" -> { plugin.getPunishmentManager().mute(target, millis); staff.sendMessage(Component.text("Muted " + target.getName() + " for " + hours + "h.")); }
                case "Temp Ban" -> { plugin.getPunishmentManager().tempBan(target, millis, "Temp-banned by staff."); staff.sendMessage(Component.text("Temp-banned " + target.getName() + " for " + hours + "h.")); }
                default -> {}
            }
            staff.closeInventory();
        }
    }
}
