package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnJailCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public UnJailCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.jail.unjail")) { sender.sendMessage("No permission."); return true; }
        if (args.length < 1) { sender.sendMessage("Usage: /unjail <player>"); return true; }
        Player t = Bukkit.getPlayerExact(args[0]);
        if (t == null) { sender.sendMessage("Player not found."); return true; }
        plugin.getPunishmentManager().unjail(t.getUniqueId());
        t.sendMessage(Component.text("You have been unjailed."));
        sender.sendMessage("Unjailed " + t.getName());
        return true;
    }
}
