package com.simplecore.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("simplecore.feed")) { p.sendMessage(Component.text("No permission.")); return true; }
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.sendMessage(Component.text("Fed."));
            return true;
        } else {
            if (!sender.hasPermission("simplecore.feed")) { sender.sendMessage("No permission."); return true; }
            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) { sender.sendMessage("Player not found."); return true; }
            t.setFoodLevel(20);
            t.setSaturation(20f);
            t.sendMessage(Component.text("Fed."));
            sender.sendMessage("Fed " + t.getName());
            return true;
        }
    }
}
