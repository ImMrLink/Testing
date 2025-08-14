package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetAfkZoneCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public SetAfkZoneCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("simplecore.afkzone.set")) { p.sendMessage(Component.text("No permission.")); return true; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /setafkzone <name> <radius> [multiplier]")); return true; }
        String name = args[0];
        double radius;
        try { radius = Double.parseDouble(args[1]); } catch (Exception ex) { p.sendMessage(Component.text("Invalid radius.")); return true; }
        double mult = args.length >= 3 ? Double.parseDouble(args[2]) : plugin.getConfig().getDouble("AFK.zone-multiplier", 2.0);
        plugin.getAfkZoneManager().addZone(name, p.getLocation(), radius, mult);
        p.sendMessage(Component.text("AFK zone '" + name + "' set. radius=" + radius + ", multiplier=" + mult));
        return true;
    }
}
