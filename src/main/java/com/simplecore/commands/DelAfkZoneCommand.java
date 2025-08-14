package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DelAfkZoneCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public DelAfkZoneCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.afkzone.del")) { sender.sendMessage("No permission."); return true; }
        if (args.length < 1) { sender.sendMessage("Usage: /delafkzone <name>"); return true; }
        boolean ok = plugin.getAfkZoneManager().removeZone(args[0]);
        sender.sendMessage(ok ? Component.text("AFK zone removed.") : Component.text("Zone not found."));
        return true;
    }
}
