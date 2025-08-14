package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class ScGroupCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public ScGroupCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.groups.admin")) { sender.sendMessage("No permission."); return true; }
        if (args.length == 0) {
            sender.sendMessage("/scgroup set <player> <group>");
            sender.sendMessage("/scgroup list");
            sender.sendMessage("/scgroup reload");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 3) { sender.sendMessage("/scgroup set <player> <group>"); return true; }
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) { sender.sendMessage("Player not found."); return true; }
                String group = args[2];
                if (plugin.getGroupManager().getGroup(group) == null) { sender.sendMessage("Unknown group."); return true; }
                plugin.getGroupManager().setPlayerGroup(p.getUniqueId(), group);
                sender.sendMessage("Set " + p.getName() + " to group " + group);
            }
            case "list" -> {
                var names = plugin.getGroupManager().getGroupNames().stream().sorted().collect(Collectors.joining(", "));
                sender.sendMessage("Groups: " + names);
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getGroupManager().load();
                plugin.getAfkZoneManager().reload();
                plugin.getAnnouncer().stop();
                plugin.getAnnouncer().start();
                plugin.getScoreboardManager().stop();
                plugin.getScoreboardManager().start();
                plugin.getDailyRewardManager().stop();
                plugin.getDailyRewardManager().start();
                plugin.getHologramManager().refresh();
                plugin.getLogWriter().reload();
                sender.sendMessage("Groups reloaded.");
            }
            default -> {}
        }
        return true;
    }
}
