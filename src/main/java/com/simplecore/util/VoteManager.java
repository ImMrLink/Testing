package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoteManager {
    private final SimpleCorePlugin plugin;

    public VoteManager(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @SuppressWarnings("unchecked")
    public void onVote(String playerName, String site) { // supports site-specific rewards
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        if (op == null) return;
        UUID id = op.getUniqueId();
        // reward voter
        java.util.List<java.util.Map<String,Object>> list = null;
        if (site != null) list = (java.util.List<java.util.Map<String,Object>>) plugin.getConfig().getList("voting.rewards.sites." + site);
        if (list == null) list = (java.util.List<java.util.Map<String,Object>>) plugin.getConfig().getList("voting.rewards.default");
        if (list == null) list = (java.util.List<java.util.Map<String,Object>>) plugin.getConfig().getList("voting.rewards");
        payout(op, list);
        if (op.isOnline()) plugin.getMissionProgress().increment(op.getPlayer(), "vote:any", 1);
        // increment counters
        String base = "votes.";
        long total = (long) plugin.getDataStoreDouble(base + "total", 0);
        plugin.getDataStore().setDouble(base + "total", total + 1);
        String today = LocalDate.now().toString();
        long day = (long) plugin.getDataStoreDouble(base + "daily."+today, 0);
        plugin.getDataStore().setDouble(base + "daily."+today, day + 1);
        long winStart = (long) plugin.getDataStoreDouble(base + "window.start", 0);
        long winCount = (long) plugin.getDataStoreDouble(base + "window.count", 0);
        long now = System.currentTimeMillis();
        long winSec = plugin.getConfig().getLong("voting.party.window-seconds", 3600);
        if (now - winStart > winSec*1000L) { winStart = now; winCount = 0; }
        plugin.getDataStore().setDouble(base + "window.start", winStart);
        plugin.getDataStore().setDouble(base + "window.count", winCount + 1);
        // check party
        checkMilestones(site);
        checkParty();
    }

    @SuppressWarnings("unchecked")
    private void payout(OfflinePlayer op, List<Map<String, Object>> list) {
        if (list == null) return;
        for (Map<String, Object> a : list) {
            String type = String.valueOf(a.getOrDefault("type", "economy")).toLowerCase(java.util.Locale.ROOT);
            switch (type) {
                case "economy" -> {
                    double amount = a.get("amount")==null?0.0:Double.parseDouble(a.get("amount").toString());
                    if (amount>0) plugin.getEconomy().deposit(op.getUniqueId(), plugin.getMultiplierManager().scaledMoney(op.getUniqueId(), amount));
                }
                case "command" -> {
                    String cmd = String.valueOf(a.getOrDefault("command", ""));
                    if (!cmd.isEmpty()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", op.getName()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkParty() {
        if (!plugin.getConfig().getBoolean("voting.party.enabled", true)) return;
        String mode = plugin.getConfig().getString("voting.party.mode", "daily-count");
        int required = plugin.getConfig().getInt("voting.party.required", 20);
        boolean trigger = false;
        String base = "votes.";
        long now = System.currentTimeMillis();
        switch (mode) {
            case "daily-count" -> {
                String today = java.time.LocalDate.now().toString();
                long day = (long) plugin.getDataStoreDouble(base + "daily."+today, 0);
                trigger = day >= required;
            }
            case "time-window" -> {
                long winStart = (long) plugin.getDataStoreDouble(base + "window.start", 0);
                long winCount = (long) plugin.getDataStoreDouble(base + "window.count", 0);
                long winSec = plugin.getConfig().getLong("voting.party.window-seconds", 3600);
                if (now - winStart > winSec*1000L) { winStart = now; winCount = 0; }
                trigger = winCount >= required;
            }
            case "total-count" -> {
                long total = (long) plugin.getDataStoreDouble(base + "total", 0);
                trigger = total >= required;
            }
        }
        if (trigger) {
            // payout party to all online
            List<Map<String, Object>> rewards = (List<Map<String, Object>>) plugin.getConfig().getList("voting.party.rewards");
            for (Player p : Bukkit.getOnlinePlayers()) payout(p, rewards);
            // reset counters depending on mode
            if ("daily-count".equals(mode)) {
                String today = java.time.LocalDate.now().toString();
                plugin.getDataStore().setDouble(base + "daily."+today, 0);
            } else if ("time-window".equals(mode)) {
                plugin.getDataStore().setDouble(base + "window.start", now);
                plugin.getDataStore().setDouble(base + "window.count", 0);
            } else {
                long total = (long) plugin.getDataStoreDouble(base + "total", 0);
                plugin.getDataStore().setDouble(base + "total", total - required);
            }
            Bukkit.broadcast(net.kyori.adventure.text.Component.text("Vote Party! Everyone has been rewarded."));
        }
    }

    public String status() {
        String mode = plugin.getConfig().getString("voting.party.mode", "daily-count");
        int required = plugin.getConfig().getInt("voting.party.required", 20);
        String base = "votes.";
        if ("daily-count".equals(mode)) {
            String today = java.time.LocalDate.now().toString();
            long day = (long) plugin.getDataStoreDouble(base + "daily."+today, 0);
            return "Mode=daily-count " + day + "/" + required;
        } else if ("time-window".equals(mode)) {
            long winCount = (long) plugin.getDataStoreDouble(base + "window.count", 0);
            long winStart = (long) plugin.getDataStoreDouble(base + "window.start", 0);
            long winSec = plugin.getConfig().getLong("voting.party.window-seconds", 3600);
            long left = Math.max(0, winSec*1000L - (System.currentTimeMillis() - winStart));
            return "Mode=time-window " + winCount + "/" + required + " (" + (left/1000) + "s left)";
        } else {
            long total = (long) plugin.getDataStoreDouble(base + "total", 0);
            return "Mode=total-count " + total + "/" + required;
        }
    }


    @SuppressWarnings("unchecked")
    private void checkMilestones(String site) {
        var section = plugin.getConfig().getConfigurationSection("voting.party.milestones.sites." + site);
        if (section == null) return;
        // milestone counts use per-day counts by default
        String today = java.time.LocalDate.now().toString();
        long day = (long) plugin.getDataStoreDouble("votes.daily."+today, 0);
        java.util.List<java.util.Map<String,Object>> list = (java.util.List<java.util.Map<String,Object>>) plugin.getConfig().getList("voting.party.milestones.sites." + site);
        if (list == null) return;
        for (var m : list) {
            int threshold = Integer.parseInt(String.valueOf(m.getOrDefault("threshold", 0)));
            boolean fired = plugin.getDataStore().usersCfg.getBoolean("votes.fired."+today+"."+site+"."+threshold, false);
            if (!fired && day >= threshold) {
                String msg = String.valueOf(m.getOrDefault("broadcast", "&aMilestone reached for {site}!"));
                msg = org.bukkit.ChatColor.translateAlternateColorCodes('&', msg.replace("{site}", site).replace("{count}", String.valueOf(day)));
                org.bukkit.Bukkit.broadcast(net.kyori.adventure.text.Component.text(msg));
                java.util.List<java.util.Map<String,Object>> rewards = (java.util.List<java.util.Map<String,Object>>) m.get("rewards");
                if (rewards != null) {
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) payout(p, rewards);
                }
                plugin.getDataStore().usersCfg.set("votes.fired."+today+"."+site+"."+threshold, true);
                plugin.getDataStore().saveAll();
            }
        }
    }

}
