package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VotePartyCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public VotePartyCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.vote.admin")) { sender.sendMessage("No permission."); return true; }
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text(plugin.getVoteManager().status()));
            return true;
        }
        if (args[0].equalsIgnoreCase("reset")) {
            plugin.getDataStore().setDouble("votes.total", 0);
            String today = java.time.LocalDate.now().toString();
            plugin.getDataStore().setDouble("votes.daily."+today, 0);
            plugin.getDataStore().setDouble("votes.window.start", System.currentTimeMillis());
            plugin.getDataStore().setDouble("votes.window.count", 0);
            sender.sendMessage(Component.text("Vote party counters reset."));
            return true;
        }
        sender.sendMessage("Usage: /voteparty <status|reset>");
        return true;
    }
}
