package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AfkZonesCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public AfkZonesCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        var zones = plugin.getAfkZoneManager().listZones();
        if (zones.isEmpty()) { sender.sendMessage(Component.text("No AFK zones.")); return true; }
        for (var z : zones) {
            sender.sendMessage(Component.text("- " + z.name + " @ " + z.center.getWorld().getName() + " " + z.center.getBlockX() + "," + z.center.getBlockY() + "," + z.center.getBlockZ() + " r=" + z.radius + " x" + z.multiplier));
        }
        return true;
    }
}
