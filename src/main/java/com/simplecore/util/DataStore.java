package com.simplecore.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final Plugin plugin;
    private final File homesFile, warpsFile, usersFile, spawnFile;
    private FileConfiguration homesCfg, warpsCfg, usersCfg, spawnCfg;

    // runtime caches
    private final Map<UUID, Location> backLoc = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastMessenger = new ConcurrentHashMap<>(); // for /reply

    public DataStore(Plugin plugin) {
        this.plugin = plugin;
        var data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();
        homesFile = new File(data, "homes.yml");
        warpsFile = new File(data, "warps.yml");
        usersFile = new File(data, "users.yml");
        spawnFile = new File(data, "spawn.yml");
    }

    public void loadAll() {
        homesCfg = YamlConfiguration.loadConfiguration(homesFile);
        warpsCfg = YamlConfiguration.loadConfiguration(warpsFile);
        usersCfg = YamlConfiguration.loadConfiguration(usersFile);
        spawnCfg = YamlConfiguration.loadConfiguration(spawnFile);
    }

    public void saveAll() {
        try { homesCfg.save(homesFile); } catch (IOException ignored) {}
        try { warpsCfg.save(warpsFile); } catch (IOException ignored) {}
        try { usersCfg.save(usersFile); } catch (IOException ignored) {}
        try { spawnCfg.save(spawnFile); } catch (IOException ignored) {}
    }

    // Spawn
    public void setSpawn(Location loc) {
        setLocation(spawnCfg, "spawn", loc);
        saveAll();
    }
    public Location getSpawn() {
        return getLocation(spawnCfg, "spawn");
    }

    // Warps
    public void setWarp(String name, Location loc) {
        setLocation(warpsCfg, "warps." + name.toLowerCase(Locale.ROOT), loc);
        saveAll();
    }
    public Location getWarp(String name) {
        return getLocation(warpsCfg, "warps." + name.toLowerCase(Locale.ROOT));
    }
    public boolean delWarp(String name) {
        String path = "warps." + name.toLowerCase(Locale.ROOT);
        if (warpsCfg.contains(path)) {
            warpsCfg.set(path, null);
            saveAll();
            return true;
        }
        return false;
    }
    public Set<String> listWarps() {
        var sec = warpsCfg.getConfigurationSection("warps");
        if (sec == null) return new HashSet<>();
        return sec.getKeys(false);
    }

    // Homes
    private String homePath(UUID uuid, String name) {
        return "homes." + uuid + "." + name.toLowerCase(Locale.ROOT);
    }
    public void setHome(UUID uuid, String name, Location loc) {
        setLocation(homesCfg, homePath(uuid, name), loc);
        saveAll();
    }
    public boolean delHome(UUID uuid, String name) {
        String path = homePath(uuid, name);
        if (homesCfg.contains(path)) {
            homesCfg.set(path, null);
            saveAll();
            return true;
        }
        return false;
    }
    public Location getHome(UUID uuid, String name) {
        return getLocation(homesCfg, homePath(uuid, name));
    }
    public Set<String> listHomes(UUID uuid) {
        var sec = homesCfg.getConfigurationSection("homes." + uuid);
        if (sec == null) return new HashSet<>();
        return sec.getKeys(false);
    }

    // Users: balances, god, fly
    public double getBalance(UUID uuid, double def) {
        return usersCfg.getDouble("users." + uuid + ".balance", def);
    }
    public void setBalance(UUID uuid, double amount) {
        usersCfg.set("users." + uuid + ".balance", amount);
        saveAll();
    }
    public boolean isGod(UUID uuid) {
        return usersCfg.getBoolean("users." + uuid + ".god", false);
    }
    public void setGod(UUID uuid, boolean val) {
        usersCfg.set("users." + uuid + ".god", val);
        saveAll();
    }
    public boolean canFly(UUID uuid) {
        return usersCfg.getBoolean("users." + uuid + ".fly", false);
    }
    public void setFly(UUID uuid, boolean val) {
        usersCfg.set("users." + uuid + ".fly", val);
        saveAll();
    }

    // Back locations
    public void setBack(UUID uuid, Location loc) { if (loc != null) backLoc.put(uuid, loc.clone()); }
    public Location getBack(UUID uuid) { return backLoc.get(uuid); }

    // Last messenger
    public void setLastMessenger(UUID receiver, UUID sender) { lastMessenger.put(receiver, sender); }
    public UUID getLastMessenger(UUID receiver) { return lastMessenger.get(receiver); }

    // Helpers
    private static void setLocation(FileConfiguration cfg, String path, Location loc) {
        if (loc == null) return;
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", loc.getYaw());
        cfg.set(path + ".pitch", loc.getPitch());
    }
    private static Location getLocation(FileConfiguration cfg, String path) {
        if (!cfg.contains(path)) return null;
        String world = cfg.getString(path + ".world");
        var w = Bukkit.getWorld(world);
        if (w == null) return null;
        double x = cfg.getDouble(path + ".x");
        double y = cfg.getDouble(path + ".y");
        double z = cfg.getDouble(path + ".z");
        float yaw = (float) cfg.getDouble(path + ".yaw");
        float pitch = (float) cfg.getDouble(path + ".pitch");
        return new Location(w, x, y, z, yaw, pitch);
    }


    public long getTime(String path, long def) {
        return usersCfg.getLong(path, def);
    }
    public void setTime(String path, long value) {
        usersCfg.set(path, value);
        saveAll();
    }

}
