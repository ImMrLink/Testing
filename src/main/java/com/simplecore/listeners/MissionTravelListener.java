package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MissionTravelListener implements Listener {
    private final SimpleCorePlugin plugin;
    private final Map<UUID, Double> accum = new HashMap<>();

    public MissionTravelListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().distanceSquared(e.getTo()) == 0) return;
        Player p = e.getPlayer();
        double dx = e.getTo().toVector().subtract(e.getFrom().toVector()).length();
        double val = accum.getOrDefault(p.getUniqueId(), 0.0) + dx;
        if (val >= 1.0) {
            int blocks = (int)Math.floor(val);
            accum.put(p.getUniqueId(), val - blocks);
            String mode = "walk";
            if (p.isGliding()) mode = "elytra";
            else if (p.isFlying() || p.getAllowFlight()) mode = "fly";
            else if (p.isSprinting()) mode = "sprint";
            else if (p.isInsideVehicle() && p.getVehicle()!=null && p.getVehicle().getType().name().contains("BOAT")) mode = "boat";
            plugin.getMissionProgress().increment(p, "travel:"+mode, blocks);
        } else {
            accum.put(p.getUniqueId(), val);
        }
    }
}
