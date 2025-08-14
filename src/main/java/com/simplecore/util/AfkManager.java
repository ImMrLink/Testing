package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.economy.EconomyService;
import com.simplecore.util.AfkZoneManager;
import com.simplecore.util.AfkRewardEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkManager {
    private final SimpleCorePlugin plugin;
    private final EconomyService economy;
    private final Map<UUID, Long> afkSince = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final Map<UUID, Double> sessionEarned = new ConcurrentHashMap<>();
    private final Map<UUID, Double> sessionEcon = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastActive = new ConcurrentHashMap<>();

    private final int autoSeconds;
    private final double rewardPerMinute;
    private final double maxPerSession;
    private final double dailyCap;
    private final int leaderboardSize;
    private final boolean broadcast;
    private final AfkRewardEngine rewardEngine;
    private final boolean rewardRequiresManual;
    private final java.util.Set<java.util.UUID> manualAfk = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public AfkManager(SimpleCorePlugin plugin) {
        this.rewardEngine = new AfkRewardEngine(plugin);
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.autoSeconds = plugin.getConfig().getInt("AFK.auto-seconds", 300);
        this.rewardPerMinute = plugin.getConfig().getDouble("AFK.reward-per-minute", 1.0);
        this.maxPerSession = plugin.getConfig().getDouble("AFK.max-per-session", 120.0);
        this.dailyCap = plugin.getConfig().getDouble("AFK.daily-cap", 500.0);
        this.leaderboardSize = plugin.getConfig().getInt("AFK.leaderboard-size", 10);
        this.broadcast = plugin.getConfig().getBoolean("AFK.broadcast", true);
        this.rewardRequiresManual = plugin.getConfig().getBoolean("AFK.reward-requires-manual", false);

        // reward task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (UUID id : afkSince.keySet()) {
                if (rewardRequiresManual && !manualAfk.contains(id)) continue;
                // zone bonus
                double mult = 1.0;
                org.bukkit.entity.Player pz = org.bukkit.Bukkit.getPlayer(id);
                if (pz != null) {
                    var zone = plugin.getAfkZoneManager().findContaining(pz.getLocation());
                if (bossbar != null && plugin.getConfig().getBoolean("zone-visuals.bossbar-enabled", true)) {
                    String title = zone != null && zone.bossbarTitle != null ? zone.bossbarTitle : plugin.getConfig().getString("ui.afk-bossbar-title", "&eAFK Rewards");
                    bossbar.name(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(org.bukkit.ChatColor.translateAlternateColorCodes('&', title)));
                }
                    if (zone != null) mult = zone.multiplier;
                }

                // daily tracking
                String today = java.time.LocalDate.now().toString();
                String dayPath = "users." + id + ".afk.day";
                String totalPath = "users." + id + ".afk.total_today";
                String lastDay = plugin.getDataStoreString(dayPath, today);
                if (!today.equals(lastDay)) {
                    plugin.setDataStoreString(dayPath, today);
                    plugin.setDataStoreDouble(totalPath, 0.0);
                }
                double totalToday = plugin.getDataStoreDouble(totalPath, 0.0);
                if (totalToday >= dailyCap) continue;

                double earned = sessionEarned.getOrDefault(id, 0.0);
                double econSession = sessionEcon.getOrDefault(id, 0.0);
                if (earned >= maxPerSession) continue;
                double remainingSession = maxPerSession - earned;
                double remainingDay = dailyCap - totalToday;
                AfkZoneManager.Zone z = plugin.getAfkZoneManager().findContaining(pz != null ? pz.getLocation() : null);
                var result = rewardEngine.payout(pz, z, remainingSession, remainingDay);
                double toGive = result.economyGiven;
                // economy deposit now handled by reward engine
                // update bossbar progress
                BossBar bar = bars.get(id);
                if (bar != null) {
                    float prog = 0f;
                    double earnedNow = sessionEarned.getOrDefault(id, 0.0);
                    if (maxPerSession > 0) prog = (float)Math.min(1.0, earnedNow / maxPerSession);
                    bar.progress(prog);
                }
                sessionEarned.put(id, earned + toGive);
                sessionEcon.put(id, econSession + toGive);
                plugin.setDataStoreDouble(totalPath, totalToday + toGive);
                plugin.setDataStoreString(dayPath, today);
                Player p = Bukkit.getPlayer(id);
                if (p != null && p.isOnline()) {
                    p.sendMessage(Component.text("AFK reward: +" + String.format("%.2f", toGive)));
                }
            }
        }, 20L * 60, 20L * 60); // every minute

        // auto-afk checker
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player p : Bukkit.getOnlinePlayers()) {
                long last = lastActive.getOrDefault(p.getUniqueId(), now);
                if (!isAfk(p.getUniqueId()) && (now - last) >= autoSeconds * 1000L) {
                    setAfk(p, true);
                }
            }
        }, 20L, 20L * 10);
    }

    public void recordActivity(UUID id) {
        lastActive.put(id, System.currentTimeMillis());
        if (isAfk(id)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) setAfk(p, false);
        }
    }

    public boolean isAfk(UUID id) {
        return afkSince.containsKey(id);
    }

    public void setAfk(Player p, boolean afk) {
        if (afk) {
            afkSince.put(p.getUniqueId(), System.currentTimeMillis());
            sessionEarned.put(p.getUniqueId(), 0.0);
            sessionEcon.put(p.getUniqueId(), 0.0);
            if (broadcast) Bukkit.broadcast(Component.text(p.getName() + " is now AFK."));
            if (plugin.getConfig().getBoolean("ui.use-titles", true)) { p.showTitle(net.kyori.adventure.title.Title.title(Component.text("AFK"), Component.text("Rewards active"))); }
            if (plugin.getConfig().getBoolean("ui.use-bossbar", true)) {
                BossBar bar = BossBar.bossBar(Component.text("AFK Rewards"), 0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
                bars.put(p.getUniqueId(), bar);
                p.showBossBar(bar);
            }
        } else {
            if (afkSince.remove(p.getUniqueId()) != null) {
                BossBar bar = bars.remove(p.getUniqueId());
                if (bar != null) p.hideBossBar(bar);
                if (plugin.getConfig().getBoolean("ui.use-titles", true)) { p.showTitle(net.kyori.adventure.title.Title.title(Component.text("Welcome back"), Component.text("AFK ended"))); }
                double earned = sessionEarned.getOrDefault(p.getUniqueId(), 0.0);
                sessionEarned.remove(p.getUniqueId());
                if (broadcast) Bukkit.broadcast(Component.text(p.getName() + " is no longer AFK. (Earned $" + String.format("%.2f", earned) + ")"));
            }
        }
    }

    public void markManual(java.util.UUID id, boolean manual) {
        if (manual) manualAfk.add(id); else manualAfk.remove(id);
    }

}
