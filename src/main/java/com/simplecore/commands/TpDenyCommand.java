package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {
    public TpDenyCommand(SimpleCorePlugin plugin) {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player target)) { sender.sendMessage("Players only."); return true; }
        UUID requesterId = TpaCommand.getRequester(target.getUniqueId());
        if (requesterId == null) { target.sendMessage(Component.text("No pending TPA requests.")); return true; }
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) requester.sendMessage(Component.text(target.getName() + " denied your TPA request."));
        target.sendMessage(Component.text("Denied TPA request."));
        TpaCommand.clear(target.getUniqueId());
        return true;
    }
}
