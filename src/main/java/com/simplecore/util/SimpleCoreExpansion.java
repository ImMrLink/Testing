package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleCoreExpansion extends PlaceholderExpansion {
    private final SimpleCorePlugin plugin;
    public SimpleCoreExpansion(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override public @NotNull String getIdentifier() { return "simplecore"; }
    @Override public @NotNull String getAuthor() { return "SimpleCore"; }
    @Override public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public boolean register() { return super.register(); }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        var id = player.getUniqueId();
        switch (params.toLowerCase()) {
            case "afk" -> { return plugin.getAfkManager().isAfk(id) ? "yes" : "no"; }
            case "zone" -> {
                if (player.isOnline()) {
                    var z = plugin.getAfkZoneManager().findContaining(player.getPlayer().getLocation());
                    return z == null ? "none" : z.name;
                } else return "none";
            }
            case "session_econ" -> { return String.format("%.2f", plugin.getAfkManager().sessionEcon.getOrDefault(id, 0.0)); }
            case "today_econ" -> { return String.format("%.2f", plugin.getDataStoreDouble("users."+id+".afk.total_today", 0.0)); }
            case "daily_cap" -> { return String.format("%.2f", plugin.getConfig().getDouble("AFK.daily-cap", 0.0)); }
            case "streak" -> { return String.valueOf(plugin.getDailyRewardManager().getStreak(id)); }
            case "daily_ready" -> {
                if (!player.isOnline()) return "no";
                return plugin.getDailyRewardManager().canClaim(player.getPlayer()) ? "yes" : "no";
            }
            case "daily_accum_hours" -> {
                long ms = plugin.getDailyRewardManager().getAccumMs(id);
                return String.valueOf(ms / 3600000);
            }
            case "prefix" -> { if (player.isOnline()) return plugin.getGroupManager().prefix(player.getPlayer()); else return ""; }
            case "suffix" -> { if (player.isOnline()) return plugin.getGroupManager().suffix(player.getPlayer()); else return ""; }
            default -> {
            if (params.startsWith("mission_percent_")) {
                String id = params.substring("mission_percent_".length());
                var m = plugin.getMissionManager().get(id);
                if (m == null) return "";
                java.util.UUID u = player.getUniqueId();
                int have = 0; int need = 0;
                for (int i=0;i<m.objectives.size();i++) { need += m.objectives.get(i).amount; have += (int) plugin.getDataStoreDouble("users."+u+".missions.progress."+m.id+".objectives."+i, 0.0);} 
                int pct = need>0 ? (int)Math.min(100, Math.round(have*100.0/need)) : 100;
                return String.valueOf(pct);
            }
            if (params.startsWith("mission_timeleft_")) {
                String id = params.substring("mission_timeleft_".length());
                var m = plugin.getMissionManager().get(id);
                if (m == null) return "";
                long ms = plugin.getMissionManager().timeLeftMillis(m);
                return plugin.getMissionProgress().formatTimeLeft(ms);
            }
            if (params.startsWith("mission_progress_")) {
                String id = params.substring("mission_progress_".length());
                var m = plugin.getMissionManager().get(id);
                if (m == null) return "";
                java.util.UUID u = player.getUniqueId();
                int have = 0; int need = 0;
                for (int i=0;i<m.objectives.size();i++) {
                    need += m.objectives.get(i).amount;
                    have += (int) plugin.getDataStoreDouble("users."+u+".missions.progress."+m.id+".objectives."+i, 0.0);
                }
                return have + "/" + need;
            }
            if (params.startsWith("mission_status_")) {
                String id = params.substring("mission_status_".length());
                var m = plugin.getMissionManager().get(id);
                if (m == null) return "unknown";
                if (plugin.getMissionProgress().isReady(player.getPlayer(), m)) return "ready";
                return "in_progress";
            }
            return null;
        }
        }
    }
}
