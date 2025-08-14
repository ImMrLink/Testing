package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public ReplyCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /reply <message>")); return true; }
        UUID last = plugin.getDataStore().getLastMessenger(p.getUniqueId());
        if (last == null) { p.sendMessage(Component.text("No one to reply to.")); return true; }
        Player target = Bukkit.getPlayer(last);
        if (target == null) { p.sendMessage(Component.text("That player is offline.")); return true; }
        String message = String.join(" ", args);
        p.sendMessage(Component.text("To " + target.getName() + ": " + message));
        target.sendMessage(Component.text("From " + p.getName() + ": " + message));
        return true;
    }
}
