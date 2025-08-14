package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.AfkManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener {
    private static final double MICRO_MOVE = 0.01; // ~0.1 blocks squared
    private static final float MICRO_ROT = 2.0f;
    private final SimpleCorePlugin plugin;
    private final AfkManager mgr;
    public AfkListener(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.mgr = plugin.getAfkManager();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        double dist2 = e.getFrom().toVector().distanceSquared(e.getTo().toVector());
        float dyaw = Math.abs(e.getTo().getYaw() - e.getFrom().getYaw());
        float dpitch = Math.abs(e.getTo().getPitch() - e.getFrom().getPitch());
        // Ignore jitter-level moves to defeat anti-AFK scripts
        if (dist2 < MICRO_MOVE && dyaw < MICRO_ROT && dpitch < MICRO_ROT) return;
        if (dist2 > 0 || dyaw > MICRO_ROT || dpitch > MICRO_ROT) {
            mgr.recordActivity(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        mgr.recordActivity(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        mgr.recordActivity(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        mgr.recordActivity(e.getPlayer().getUniqueId());
    }
}
