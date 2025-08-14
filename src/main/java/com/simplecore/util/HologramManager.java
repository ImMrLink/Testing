package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {
    private final SimpleCorePlugin plugin;
    private final Map<String, java.util.UUID> spawned = new HashMap<>();

    public HologramManager(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void refresh() {
        // remove old
        for (var id : spawned.values()) {
            var e = get(id);
            if (e != null) e.remove();
        }
        spawned.clear();
        if (!plugin.getConfig().getBoolean("zone-visuals.holograms-enabled", true)) return;
        for (var z : plugin.getAfkZoneManager().listZones()) {
            if (z.hologramText == null || z.hologramText.isEmpty()) continue;
            ArmorStand as = (ArmorStand) z.center.getWorld().spawnEntity(z.center.clone().add(0, 1.8, 0), EntityType.ARMOR_STAND);
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.setCustomNameVisible(true);
            as.customName(org.bukkit.ChatColor.translateAlternateColorCodes('&', z.hologramText));
            spawned.put(z.name.toLowerCase(java.util.Locale.ROOT), as.getUniqueId());
        }
    }

    private ArmorStand get(java.util.UUID id) {
        var e = Bukkit.getEntity(id);
        if (e instanceof ArmorStand as) return as;
        return null;
    }
}
