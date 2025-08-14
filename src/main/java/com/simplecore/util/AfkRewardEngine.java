package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AfkRewardEngine {
    public static class Result {
        public double economyGiven = 0.0; // amount actually deposited this tick
    }

    private final SimpleCorePlugin plugin;

    public AfkRewardEngine(SimpleCorePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(com.simplecore.util.AfkZoneManager.Zone zone, boolean inZone) {
        if (zone != null && zone.rewards != null) return (List<Map<String,Object>>) (List<?>) zone.rewards;
        String path = inZone ? "AFK.rewards.in-zone" : "AFK.rewards.out-of-zone";
        List<?> raw = plugin.getConfig().getList(path);
        if (raw == null) return java.util.List.of();
        return (List<Map<String, Object>>) (List<?>) raw;
    }

    public Result payout(Player p, com.simplecore.util.AfkZoneManager.Zone zone, double remainingSession, double remainingDay) {
        Result r = new Result();
        boolean inZone = zone != null;
        java.util.List<java.util.Map<String,Object>> actions = (zone != null && zone.rewards != null && !zone.rewards.isEmpty()) ? (java.util.List<java.util.Map<String,Object>>)zone.rewards : getList(inZone);
        boolean useMult = plugin.getConfig().getBoolean("AFK.use-zone-multiplier-on-economy", true);
        double mult = (inZone && useMult) ? zone.multiplier : 1.0;

        for (Map<String,Object> action : actions) {
            String type = String.valueOf(action.getOrDefault("type", "economy")).toLowerCase(Locale.ROOT);
            switch (type) {
                case "economy" -> {
                    double perMin = 0.0;
                    Object apm = action.get("amount-per-minute");
                    if (apm != null) perMin = Double.parseDouble(apm.toString());
                    double toGive = perMin * mult;
                    // obey caps if present
                    toGive = Math.max(0.0, Math.min(toGive, Math.min(remainingSession, remainingDay)));
                    if (toGive > 0.0) {
                        plugin.getEconomy().deposit(p.getUniqueId(), plugin.getMultiplierManager().scaledMoney(p.getUniqueId(), toGive));
                        r.economyGiven += toGive;
                        p.sendMessage(net.kyori.adventure.text.Component.text("AFK reward: +" + String.format("%.2f", toGive)));
                    }
                }
                case "command" -> {
                    String cmd = String.valueOf(action.getOrDefault("command", ""));
                    int times = 1;
                    Object pm = action.get("per-minute");
                    if (pm != null) times = Math.max(1, Integer.parseInt(pm.toString()));
                    if (!cmd.isEmpty()) {
                        String zoneName = zone != null ? zone.name : "none";
                        for (int i=0;i<times;i++) {
                            String built = cmd.replace("{player}", p.getName()).replace("{zone}", zoneName);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), built);
                        }
                    }
                }
            }
        }
        return r;
    }
}
