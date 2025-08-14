package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteAddCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public VoteAddCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.vote.admin")) { sender.sendMessage("No permission."); return true; }
        if (args.length < 1) { sender.sendMessage("Usage: /voteadd <player> [site]"); return true; }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
        if (op == null) { sender.sendMessage("Player not found."); return true; }
        plugin.getVoteManager().onVote(op.getName(), args.length>=2?args[1]:"manual");
        sender.sendMessage(Component.text("Vote registered for " + op.getName()));
        return true;
    }
}
