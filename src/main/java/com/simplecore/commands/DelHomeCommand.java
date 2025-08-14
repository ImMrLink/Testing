package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public DelHomeCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /delhome <name>")); return true; }
        String name = args[0];
        boolean ok = plugin.getDataStore().delHome(p.getUniqueId(), name);
        p.sendMessage(Component.text(ok ? "Deleted home '" + name + "'." : "Home not found."));
        return true;
    }
}
