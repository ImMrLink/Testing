package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.DataStore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MsgCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public MsgCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /msg <player> <message>")); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { p.sendMessage(Component.text("Player not found.")); return true; }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        p.sendMessage(Component.text("To " + target.getName() + ": " + message));
        target.sendMessage(Component.text("From " + p.getName() + ": " + message));
        plugin.getDataStore().setLastMessenger(p.getUniqueId(), target.getUniqueId());
        plugin.getDataStore().setLastMessenger(target.getUniqueId(), p.getUniqueId());
        return true;
    }
}
