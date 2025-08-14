package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public SetHomeCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /sethome <name>")); return true; }
        String name = args[0];
        plugin.getDataStore().setHome(p.getUniqueId(), name, p.getLocation());
        p.sendMessage(Component.text("Home '" + name + "' set."));
        return true;
    }
}
