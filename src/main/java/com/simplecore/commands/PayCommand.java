package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public PayCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /pay <player> <amount>")); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { p.sendMessage(Component.text("Player not found.")); return true; }
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (Exception e) { p.sendMessage(Component.text("Invalid amount.")); return true; }
        if (amount <= 0) { p.sendMessage(Component.text("Amount must be positive.")); return true; }
        if (!plugin.getEconomy().withdraw(p.getUniqueId(), amount)) {
            p.sendMessage(Component.text("Insufficient funds.")); return true;
        }
        plugin.getEconomy().deposit(target.getUniqueId(), amount);
        p.sendMessage(Component.text("Paid " + target.getName() + " $" + String.format("%.2f", amount)));
        target.sendMessage(Component.text("You received $" + String.format("%.2f", amount) + " from " + p.getName()));
        return true;
    }
}
