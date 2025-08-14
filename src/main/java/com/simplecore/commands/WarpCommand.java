package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public WarpCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /warp <name>")); return true; }
        var loc = plugin.getDataStore().getWarp(args[0]);
        if (loc == null) { p.sendMessage(Component.text("Warp not found.")); return true; }
        p.teleportAsync(loc).thenRun(() -> p.sendMessage(Component.text("Warped to " + args[0])));
        return true;
    }
}
