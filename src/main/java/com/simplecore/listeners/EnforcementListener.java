package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EnforcementListener implements Listener {
    private final SimpleCorePlugin plugin;
    public EnforcementListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (plugin.getPunishmentManager().isMuted(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "You are muted.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        var pm = plugin.getPunishmentManager();
        if (!pm.isJailed(e.getPlayer().getUniqueId())) return;
        var jail = pm.getJail();
        if (jail == null) return;
        if (e.getTo() == null) return;
        if (!e.getTo().getWorld().equals(jail.getWorld())) {
            e.getPlayer().teleportAsync(jail);
            return;
        }
        if (e.getTo().distanceSquared(jail) > pm.jailRadius() * pm.jailRadius()) {
            e.setTo(jail);
        }
    }
}
