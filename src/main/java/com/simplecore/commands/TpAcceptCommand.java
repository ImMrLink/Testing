package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public TpAcceptCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player target)) { sender.sendMessage("Players only."); return true; }
        UUID requesterId = TpaCommand.getRequester(target.getUniqueId());
        if (requesterId == null) { target.sendMessage(Component.text("No pending TPA requests.")); return true; }
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) { target.sendMessage(Component.text("Requester is offline.")); TpaCommand.clear(target.getUniqueId()); return true; }

        Location dest = target.getLocation();
        requester.teleportAsync(dest).thenRun(() -> {
            requester.sendMessage(Component.text("Teleported to " + target.getName()));
            target.sendMessage(Component.text(requester.getName() + " has teleported to you."));
            TpaCommand.clear(target.getUniqueId());
        });
        return true;
    }
}
