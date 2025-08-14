package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    public java.util.Set<java.util.UUID> disabled = new java.util.HashSet<>();
    private final SimpleCorePlugin plugin;
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> boards = new HashMap<>();
    private BukkitTask task;

    public ScoreboardManager(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void start() {
        disabled.clear();
        stop();
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        boolean pref = plugin.getDataStore().usersCfg.getBoolean("users."+p.getUniqueId()+".prefs.scoreboard", true);
        if (!pref) { disableFor(p); return; }
        int ticks = Math.max(20, plugin.getConfig().getInt("scoreboard.refresh-ticks", 40));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, ticks);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getScoreboard() == boards.get(p.getUniqueId())) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        boards.clear();
    }

    private void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
        }
    }

    public void update(Player p) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        boolean pref = plugin.getDataStore().usersCfg.getBoolean("users."+p.getUniqueId()+".prefs.scoreboard", true);
        if (!pref) { disableFor(p); return; }
        var mgr = Bukkit.getScoreboardManager();
        if (mgr == null) return;
        var board = boards.computeIfAbsent(p.getUniqueId(), k -> mgr.getNewScoreboard());
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "&aSimpleCore"));
        var obj = board.getObjective("simplecore");
        if (obj == null) obj = board.registerNewObjective("simplecore", "dummy", title);
        obj.setDisplayName(title);
        obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        // Clear and rebuild
        for (String entry : board.getEntries()) board.resetScores(entry);

        java.util.List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        if (lines == null || lines.isEmpty()) return;

        // placeholders
        String afk = plugin.getAfkManager().isAfk(p.getUniqueId()) ? "Yes" : "No";
        String zoneName = "none";
        com.simplecore.util.AfkZoneManager.Zone z = plugin.getAfkZoneManager().findContaining(p.getLocation());
        if (z != null) zoneName = z.name + " x" + z.multiplier;
        double session = plugin.getAfkManager().sessionEcon.getOrDefault(p.getUniqueId(), 0.0);
        double today = plugin.getDataStoreDouble("users." + p.getUniqueId() + ".afk.total_today", 0.0);
        double cap = plugin.getConfig().getDouble("AFK.daily-cap", 0.0);

        int score = lines.size();
        int uniquifier = 0;
        for (String raw : lines) {
            String s = raw.replace("{afk}", afk)
                    .replace("{zone}", zoneName)
                    .replace("{session_econ}", String.format("%.2f", session))
                    .replace("{today_econ}", String.format("%.2f", today))
                    .replace("{daily_cap}", String.format("%.2f", cap));
            s = ChatColor.translateAlternateColorCodes('&', s);
            // Ensure unique entries
            while (board.getEntries().contains(s)) {
                s = s + ChatColor.RESET;
                uniquifier++;
                if (uniquifier > 10) break;
            }
            obj.getScore(s).setScore(score--);
        }
        p.setScoreboard(board);
    }

    public void disableFor(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        boards.remove(p.getUniqueId());
    }
}
