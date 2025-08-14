package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MultiplierCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public MultiplierCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.multiplier.admin")) { sender.sendMessage("No permission."); return true; }
        if (args.length < 1) { sender.sendMessage("Usage: /multiplier <give|clear|show> ..."); return true; }
        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 5) { sender.sendMessage("Usage: /multiplier give <player|all> <moneyFactor> <expFactor> <durationMinutes>"); return; }
                String target = args[1];
                double money = Double.parseDouble(args[2]);
                double exp = Double.parseDouble(args[3]);
                long durMs = Long.parseLong(args[4]) * 60_000L;
                if (target.equalsIgnoreCase("all")) {
                    for (Player p : Bukkit.getOnlinePlayers()) plugin.getMultiplierManager().give(p.getUniqueId(), money, exp, durMs);
                    sender.sendMessage(Component.text("Granted to all online."));
                } else {
                    Player p = Bukkit.getPlayer(target);
                    if (p == null) { sender.sendMessage("Player not found."); return; }
                    plugin.getMultiplierManager().give(p.getUniqueId(), money, exp, durMs);
                    sender.sendMessage(Component.text("Granted to " + p.getName()));
                }
            }
            case "clear" -> {
                if (args.length < 2) { sender.sendMessage("Usage: /multiplier clear <player>"); return; }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) { sender.sendMessage("Player not found."); return; }
                plugin.getMultiplierManager().clear(p.getUniqueId());
                sender.sendMessage(Component.text("Cleared multipliers for " + p.getName()));
            }
            case "show" -> {
                if (args.length < 2) { sender.sendMessage("Usage: /multiplier show <player>"); return; }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) { sender.sendMessage("Player not found."); return; }
                double pm = plugin.getMultiplierManager().permMoney(p.getUniqueId());
                double gm = plugin.getMultiplierManager().grantMoney(p.getUniqueId());
                double pe = plugin.getMultiplierManager().permExp(p.getUniqueId());
                double ge = plugin.getMultiplierManager().grantExp(p.getUniqueId());
                sender.sendMessage(Component.text("Money: perm=" + pm + " grant=" + gm + " -> effective=" + Math.max(pm,gm)));
                sender.sendMessage(Component.text("EXP:   perm=" + pe + " grant=" + ge + " -> effective=" + Math.max(pe,ge)));
            }
            default -> sender.sendMessage("Usage: /multiplier <give|clear|show> ...");
        }
        return true;
    }
}
