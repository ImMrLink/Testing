package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public SetWarpCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("simplecore.setwarp")) { p.sendMessage(Component.text("No permission.")); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /setwarp <name>")); return true; }
        plugin.getDataStore().setWarp(args[0], p.getLocation());
        p.sendMessage(Component.text("Warp '" + args[0] + "' set."));
        return true;
    }
}
