package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AfkZoneManager {
    public static class Zone {
        public final java.util.List<java.util.Map<String, Object>> rewards;
        public final String name;
        public final Location center;
        public final double radius;
        public final double multiplier;
        public Zone(String name, Location center, double radius, double multiplier, java.util.List<java.util.Map<String,Object>> rewards) {
            this.name = name; this.center = center; this.radius = radius; this.multiplier = multiplier; this.rewards = rewards;
        }
        public boolean contains(Location loc) {
            if (loc == null || center == null) return false;
            if (!loc.getWorld().getUID().equals(center.getWorld().getUID())) return false;
            return loc.distanceSquared(center) <= radius * radius;
        }
    }

    private final SimpleCorePlugin plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<String, Zone> zones = new HashMap<>();

    public AfkZoneManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "afkzones.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        this.cfg = YamlConfiguration.loadConfiguration(file);
        zones.clear();
        if (cfg.isConfigurationSection("zones")) {
            for (String key : cfg.getConfigurationSection("zones").getKeys(false)) {
                String w = cfg.getString("zones." + key + ".world");
                double x = cfg.getDouble("zones." + key + ".x");
                double y = cfg.getDouble("zones." + key + ".y");
                double z = cfg.getDouble("zones." + key + ".z");
                double r = cfg.getDouble("zones." + key + ".radius", 5.0);
                double m = cfg.getDouble("zones." + key + ".multiplier", plugin.getConfig().getDouble("AFK.zone-multiplier", 2.0));
                var world = Bukkit.getWorld(w);
                if (world != null) {
                    java.util.List<java.util.Map<String, Object>> rw = (java.util.List<java.util.Map<String, Object>>) cfg.getList("zones." + key + ".rewards");
                zones.put(key.toLowerCase(java.util.Locale.ROOT), new Zone(key, new Location(world, x, y, z), r, m, rw));
                }
            }
        }
    }

    public void save() {
        try { cfg.save(file); } catch (IOException ignored) {}
    }

    public boolean addZone(String name, Location loc, double radius, double multiplier) {
        name = name.toLowerCase(java.util.Locale.ROOT);
        cfg.set("zones." + name + ".world", loc.getWorld().getName());
        cfg.set("zones." + name + ".x", loc.getX());
        cfg.set("zones." + name + ".y", loc.getY());
        cfg.set("zones." + name + ".z", loc.getZ());
        cfg.set("zones." + name + ".radius", radius);
        cfg.set("zones." + name + ".multiplier", multiplier);
        save();
        zones.put(name, new Zone(name, loc, radius, multiplier));
        return true;
    }

    public boolean removeZone(String name) {
        name = name.toLowerCase(java.util.Locale.ROOT);
        if (!zones.containsKey(name)) return false;
        cfg.set("zones." + name, null);
        save();
        zones.remove(name);
        return true;
    }

    public java.util.Collection<Zone> listZones() { return zones.values(); }

    public Zone findContaining(org.bukkit.Location loc) {
        for (Zone z : zones.values()) if (z.contains(loc)) return z;
        return null;
    }
}
