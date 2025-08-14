package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.KitManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public KitCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /kit <name>")); return true; }
        String name = args[0].toLowerCase();
        if (!p.hasPermission("simplecore.kit." + name) && !p.isOp()) {
            p.sendMessage(Component.text("No permission: simplecore.kit." + name));
            return true;
        }
        KitManager km = plugin.getKitManager();
        if (km.getKit(name) == null) {
            p.sendMessage(Component.text("Kit not found."));
            return true;
        }
        km.give(p, name);
        return true;
    }
}
