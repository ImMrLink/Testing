package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiplierManager {
    public static class Entry {
        public final double money;
        public final double exp;
        public final long until; // ms
        public Entry(double money, double exp, long until) { this.money = money; this.exp = exp; this.until = until; }
        public boolean active() { return System.currentTimeMillis() < until; }
    }

    private final SimpleCorePlugin plugin;
    private final Map<UUID, List<Entry>> grants = new ConcurrentHashMap<>();

    public MultiplierManager(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void give(UUID id, double money, double exp, long durationMs) {
        grants.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(new Entry(money, exp, System.currentTimeMillis()+durationMs));
    }

    public void clear(UUID id) { grants.remove(id); }

    public double permMoney(UUID id) {
        Player p = Bukkit.getPlayer(id);
        if (p == null) return plugin.getConfig().getDouble("multipliers.default-money",1.0);
        double best = 1.0;
        for (var pai : p.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase(java.util.Locale.ROOT);
            if (perm.startsWith("simplecore.multiplier.money.")) {
                try { double f = Double.parseDouble(perm.substring("simplecore.multiplier.money.".length())); best = Math.max(best, f); } catch (Exception ignored) {}
            }
        }
        return Math.max(best, plugin.getConfig().getDouble("multipliers.default-money",1.0));
    }

    public double permExp(UUID id) {
        Player p = Bukkit.getPlayer(id);
        if (p == null) return plugin.getConfig().getDouble("multipliers.default-exp",1.0);
        double best = 1.0;
        for (var pai : p.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase(java.util.Locale.ROOT);
            if (perm.startsWith("simplecore.multiplier.exp.")) {
                try { double f = Double.parseDouble(perm.substring("simplecore.multiplier.exp.".length())); best = Math.max(best, f); } catch (Exception ignored) {}
            }
        }
        return Math.max(best, plugin.getConfig().getDouble("multipliers.default-exp",1.0));
    }

    public double grantMoney(UUID id) {
        double best = 1.0;
        for (Entry e : grants.getOrDefault(id, java.util.List.of())) if (e.active()) best = Math.max(best, e.money);
        return best;
    }

    public double grantExp(UUID id) {
        double best = 1.0;
        for (Entry e : grants.getOrDefault(id, java.util.List.of())) if (e.active()) best = Math.max(best, e.exp);
        return best;
    }

    public double scaledMoney(UUID id, double base) {
        double f = Math.max(permMoney(id), grantMoney(id));
        return base * f;
    }

    public int scaledExp(UUID id, int base) {
        double f = Math.max(permExp(id), grantExp(id));
        return (int)Math.round(base * f);
    }
}
