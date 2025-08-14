package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class AfkTopCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public AfkTopCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int n = plugin.getConfig().getInt("AFK.leaderboard-size", 10);
        if (args.length > 0) {
            try { n = Math.max(1, Math.min(50, Integer.parseInt(args[0]))); } catch (Exception ignored) {}
        }
        String today = java.time.LocalDate.now().toString();
        Map<String, Double> map = new HashMap<>();
        for (String idStr : plugin.getDataStore().getUserIds()) {
            String dayPath = "users." + idStr + ".afk.day";
            String totalPath = "users." + idStr + ".afk.total_today";
            String lastDay = plugin.getDataStoreString(dayPath, today);
            double total = plugin.getDataStoreDouble(totalPath, 0.0);
            if (!today.equals(lastDay) || total <= 0) continue;
            map.put(idStr, total);
        }
        var top = map.entrySet().stream()
                .sorted((a,b)->Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
        if (top.isEmpty()) {
            sender.sendMessage("No AFK rewards yet today.");
            return true;
        }
        sender.sendMessage("AFK Top (" + today + "):");
        int rank = 1;
        for (var e : top) {
            try {
                UUID id = java.util.UUID.fromString(e.getKey());
                OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                String name = op != null && op.getName() != null ? op.getName() : id.toString().substring(0,8);
                sender.sendMessage("#" + rank + " " + name + " - $" + String.format("%.2f", e.getValue()));
                rank++;
            } catch (Exception ex) {
                sender.sendMessage("#" + rank + " " + e.getKey().substring(0,8) + " - $" + String.format("%.2f", e.getValue()));
                rank++;
            }
        }
        return true;
    }
}
