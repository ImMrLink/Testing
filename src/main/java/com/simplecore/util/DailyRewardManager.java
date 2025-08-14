package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DailyRewardManager {
    private final SimpleCorePlugin plugin;
    private int taskId = -1;

    public DailyRewardManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("daily-rewards.enabled", true)) return;
        // tick every minute to accumulate online time
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L * 60, 20L * 60).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String base = "users." + p.getUniqueId() + ".daily.";
            long last = plugin.getDataStoreTime(base + "last_tick", now);
            if (last <= 0) last = now;
            long delta = Math.max(0, now - last);
            long accum = (long) plugin.getDataStoreDouble(base + "accum_ms", 0.0);
            plugin.setDataStoreTime(base + "last_tick", now);
            plugin.setDataStoreDouble(base + "accum_ms", accum + delta);
        }
    }

    public boolean canClaim(Player p) {
        String base = "users." + p.getUniqueId() + ".daily.";
        LocalDate today = LocalDate.now();
        String lastDay = plugin.getDataStoreString(base + "last_claim_day", "");
        if (!today.toString().equals(lastDay)) {
            // new calendar day: always claimable
            return true;
        }
        // else allow if accum since last claim >= required hours
        long accum = (long) plugin.getDataStoreDouble(base + "accum_ms", 0.0);
        long reqMs = (long)(plugin.getConfig().getInt("daily-rewards.required-online-hours", 24) * 3600L * 1000L);
        return accum >= reqMs;
    }

    @SuppressWarnings("unchecked")
    public void give(Player p) {
        String base = "users." + p.getUniqueId() + ".daily.";
        // streak
        LocalDate today = LocalDate.now();
        String lastDay = plugin.getDataStoreString(base + "last_claim_day", "");
        int streak = (int) plugin.getDataStoreDouble(base + "streak", 0.0);

        if (lastDay.isEmpty()) {
            streak = 1;
        } else {
            LocalDate last = LocalDate.parse(lastDay);
            if (today.equals(last)) {
                // same day claim again: streak unchanged
            } else if (last.plusDays(1).equals(today)) {
                streak += 1;
            } else {
                int missWindow = plugin.getConfig().getInt("daily-rewards.reset-streak-if-missed-days", 2);
                if (last.plusDays(missWindow).isBefore(today)) streak = 1; // hard reset after window
                else streak += 1;
            }
        }

        // base rewards
        List<Map<String, Object>> baseRewards = (List<Map<String, Object>>) plugin.getConfig().getList("daily-rewards.rewards.base");
        payoutList(p, baseRewards);

        // streak tiers
        var tiers = plugin.getConfig().getConfigurationSection("daily-rewards.rewards.streak-tiers");
        if (tiers != null) {
            for (String key : tiers.getKeys(false)) {
                try {
                    int at = Integer.parseInt(key);
                    if (streak >= at) {
                        List<Map<String, Object>> tier = (List<Map<String, Object>>) plugin.getConfig().getList("daily-rewards.rewards.streak-tiers." + key);
                        payoutList(p, tier);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // persist
        plugin.setDataStoreString(base + "last_claim_day", today.toString());
        plugin.setDataStoreDouble(base + "streak", streak);
        plugin.setDataStoreDouble(base + "accum_ms", 0.0);
        plugin.setDataStoreTime(base + "last_tick", System.currentTimeMillis());
        if (p.isOnline()) plugin.getMissionProgress().increment(p, "daily_claim:any", 1);
    }

    private void payoutList(Player p, List<Map<String, Object>> list) {
        if (list == null) return;
        for (Map<String,Object> action : list) {
            String type = String.valueOf(action.getOrDefault("type", "economy")).toLowerCase(java.util.Locale.ROOT);
            switch (type) {
                case "economy" -> {
                    double amount = 0.0;
                    Object a = action.get("amount");
                    if (a != null) amount = Double.parseDouble(a.toString());
                    if (amount > 0) plugin.getEconomy().deposit(p.getUniqueId(), plugin.getMultiplierManager().scaledMoney(p.getUniqueId(), amount));
                }
                case "command" -> {
                    String cmd = String.valueOf(action.getOrDefault("command", ""));
                    if (!cmd.isEmpty()) {
                        String built = cmd.replace("{player}", p.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), built);
                    }
                }
            }
        }
    }

    public int getStreak(UUID id) {
        return (int) plugin.getDataStoreDouble("users." + id + ".daily.streak", 0.0);
    }

    public long getAccumMs(UUID id) {
        return (long) plugin.getDataStoreDouble("users." + id + ".daily.accum_ms", 0.0);
    }

    public String getLastClaimDay(UUID id) {
        return plugin.getDataStoreString("users." + id + ".daily.last_claim_day", "");
    }
}
