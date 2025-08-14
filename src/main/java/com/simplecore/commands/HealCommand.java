package com.simplecore.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("simplecore.heal")) { p.sendMessage(Component.text("No permission.")); return true; }
            var maxAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double max = maxAttr != null ? maxAttr.getValue() : 20;
            p.setHealth(Math.min(max, max));
            p.setFireTicks(0);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.sendMessage(Component.text("Healed."));
            return true;
        } else {
            if (!sender.hasPermission("simplecore.heal")) { sender.sendMessage("No permission."); return true; }
            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) { sender.sendMessage("Player not found."); return true; }
            var maxAttr = t.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double max = maxAttr != null ? maxAttr.getValue() : 20;
            t.setHealth(Math.min(max, max));
            t.setFireTicks(0);
            t.setFoodLevel(20);
            t.setSaturation(20f);
            t.sendMessage(Component.text("Healed."));
            sender.sendMessage("Healed " + t.getName());
            return true;
        }
    }
}
