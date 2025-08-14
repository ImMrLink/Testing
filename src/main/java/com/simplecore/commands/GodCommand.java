package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public GodCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("simplecore.god")) { p.sendMessage(Component.text("No permission.")); return true; }
            toggle(p);
            return true;
        } else {
            if (!sender.hasPermission("simplecore.god")) { sender.sendMessage("No permission."); return true; }
            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) { sender.sendMessage("Player not found."); return true; }
            toggle(t);
            sender.sendMessage("Toggled god for " + t.getName());
            return true;
        }
    }

    private void toggle(Player p) {
        boolean newVal = !p.isInvulnerable();
        p.setInvulnerable(newVal);
        plugin.getDataStore().setGod(p.getUniqueId(), newVal);
        p.sendMessage(Component.text("God mode: " + (newVal ? "enabled" : "disabled")));
    }
}
