package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DelWarpCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public DelWarpCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) { sender.sendMessage("Usage: /delwarp <name>"); return true; }
        if (!sender.hasPermission("simplecore.delwarp")) { sender.sendMessage("No permission."); return true; }
        boolean ok = plugin.getDataStore().delWarp(args[0]);
        sender.sendMessage(ok ? "Deleted warp '" + args[0] + "'." : "Warp not found.");
        return true;
    }
}
