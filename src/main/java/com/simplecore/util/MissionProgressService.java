package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionProgressService {
    private int tickerTask = -1; // minute tick

    private final SimpleCorePlugin plugin;
    private final MissionManager mm;

    public MissionProgressService(SimpleCorePlugin plugin) {
        this.plugin = plugin; this.mm = plugin.getMissionManager();
    }

    public void increment(Player p, String objectiveKey, int delta) {
        if (p == null) return;
        for (var m : mm.all()) {
            if (!mm.isUnlocked(p, m)) continue;
            for (int i=0;i<m.objectives.size();i++) {
                MissionManager.Objective obj = m.objectives.get(i);
                if (matches(obj, objectiveKey)) {
                    String base = "users."+p.getUniqueId()+".missions.progress."+m.id+".";
                    int cur = (int) plugin.getDataStoreDouble(base+"objectives."+i, 0.0);
                    int next = cur + delta;
                    plugin.setDataStoreDouble(base+"objectives."+i, next);
                    // actionbar feedback minimal
                    p.sendActionBar(net.kyori.adventure.text.Component.text(m.displayName + ": " + next + "/" + obj.amount));
                }
            }
        }
    }

    private boolean matches(MissionManager.Objective obj, String key) {
        // key examples: break:DIAMOND_ORE, break:#any, vote:any, crate_open:daily, daily_claim:any
        if (obj.type.equalsIgnoreCase(key)) return true;
        // tag match support (#any, #ores)
        String[] o = obj.type.split(":", 2);
        String[] k = key.split(":", 2);
        if (o.length==2 && k.length==2) {
            if (o[0].equalsIgnoreCase(k[0])) {
                if (o[1].startsWith("#")) {
                    String tag = o[1].substring(1).toLowerCase(java.util.Locale.ROOT);
                    if (tag.equals("any")) return true;
                    if (o[0].equalsIgnoreCase("ore") || (o[0].equalsIgnoreCase("break") && tag.equals("ores"))) {
                        return k[1].toUpperCase(java.util.Locale.ROOT).contains("ORE");
                    }
                }
            }
        }
        return false;
    }

    public boolean isReady(Player p, MissionManager.Mission m) {
        if (!plugin.getMissionManager().isUnlocked(p, m)) return false;
        for (int i=0;i<m.objectives.size();i++) {
            int cur = (int) plugin.getDataStoreDouble("users."+p.getUniqueId()+".missions.progress."+m.id+".objectives."+i, 0.0);
            if (cur < m.objectives.get(i).amount) return false;
        }
        return true;
    }

    public void claim(Player p, MissionManager.Mission m) {
        if (!isReady(p, m)) { p.sendMessage(net.kyori.adventure.text.Component.text("Mission not ready.")); return; }
        // rewards
        for (var rw : m.rewards) {
            String t = rw.type.toLowerCase(java.util.Locale.ROOT);
            switch (t) {
                case "economy" -> {
                    double amount = rw.data.get("amount")==null?0.0:Double.parseDouble(rw.data.get("amount").toString());
                    if (amount > 0) plugin.getEconomy().deposit(p.getUniqueId(), plugin.getMultiplierManager().scaledMoney(p.getUniqueId(), amount));
                }
                case "command" -> {
                    String cmd = String.valueOf(rw.data.getOrDefault("command", ""));
                    if (!cmd.isEmpty()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", p.getName()));
                }
                case "item" -> {
                    String mat = String.valueOf(rw.data.getOrDefault("material", "STONE"));
                    int amt = Integer.parseInt(String.valueOf(rw.data.getOrDefault("amount", 1)));
                    try { var is = new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mat), amt); p.getInventory().addItem(is); } catch (Exception ignored) {}
                }
                case "kit" -> {
                    String kit = String.valueOf(rw.data.getOrDefault("kit", ""));
                    if (!kit.isEmpty()) {
                        try { plugin.getKitManager().give(p, kit, true); } catch (Throwable ignored) {}
                    }
                }
                case "cratekey" -> {
                    String crate = String.valueOf(rw.data.getOrDefault("crate", ""));
                    int amt = Integer.parseInt(String.valueOf(rw.data.getOrDefault("amount", 1)));
                    if (!crate.isEmpty())
                        plugin.getCrateManager().addVirtualKeys(p.getUniqueId(), crate, amt);
                }
            }
        }
        // mark as completed
        plugin.setDataStoreDouble("users."+p.getUniqueId()+".missions.completed."+m.id+"."+plugin.getMissionManager().scopeKey(m), 1.0);
        // reset progress for daily/weekly by clearing objectives
        for (int i=0;i<m.objectives.size();i++) plugin.setDataStoreDouble("users."+p.getUniqueId()+".missions.progress."+m.id+".objectives."+i, 0.0);
        // advance chain
        if (m.chain != null && m.next != null) {
            plugin.setDataStoreString("users."+p.getUniqueId()+".missions.chains."+m.chain+".current", m.next);
        }
        p.sendMessage(net.kyori.adventure.text.Component.text("Mission claimed: " + m.displayName));
        // log
        plugin.getLogWriter().logKit(p.getUniqueId(), "MISSION:"+m.id, "claimed", ""); // reuse log format
    }

    private boolean checkFilters(org.bukkit.entity.Player p, MissionManager.Mission m) {
        // world allow/deny
        String world = p.getWorld().getName();
        if (!m.filters.worldsAllow.isEmpty() && !m.filters.worldsAllow.contains(world)) return false;
        if (m.filters.worldsDeny.contains(world)) return false;
        // regions deny via WorldGuard if present
        if (!m.filters.regionsDeny.isEmpty()) {
            try {
                Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
                com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
                if (wg != null) {
                    var loc = p.getLocation();
                    var container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
                    var query = container.createQuery();
                    var set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(loc));
                    for (var r : set) if (m.filters.regionsDeny.contains(r.getId())) return false;
                }
            } catch (Throwable ignored) {}
        }
        return true;
    }

    public String formatTimeLeft(long ms) {
        if (ms < 0) return "—";
        long s = ms / 1000;
        long h = s / 3600; s %= 3600;
        long m = s / 60; s %= 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    public void startTickers() {
        if (tickerTask != -1) org.bukkit.Bukkit.getScheduler().cancelTask(tickerTask);
        tickerTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                // playtime minute
                increment(p, "playtime:minutes", 1);
                // AFK global
                if (plugin.getAfkManager().isAfk(p.getUniqueId())) {
                    increment(p, "afk:minutes", 1);
                    var z = plugin.getAfkZoneManager().findContaining(p.getLocation());
                    if (z != null) increment(p, "afk_zone:"+z.name+":minutes", 1);
                }
            }
        }, 20L*60, 20L*60).getTaskId();
    }
}
