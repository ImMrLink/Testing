package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public FlyCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
            if (!p.hasPermission("simplecore.fly")) { p.sendMessage(Component.text("No permission.")); return true; }
            toggle(p);
            return true;
        } else {
            if (!sender.hasPermission("simplecore.fly")) { sender.sendMessage("No permission."); return true; }
            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) { sender.sendMessage("Player not found."); return true; }
            toggle(t);
            sender.sendMessage("Toggled fly for " + t.getName());
            return true;
        }
    }

    private void toggle(Player p) {
        boolean newVal = !p.getAllowFlight();
        p.setAllowFlight(newVal || p.getGameMode() == GameMode.CREATIVE);
        if (p.getGameMode() == GameMode.SURVIVAL && !newVal) p.setFlying(false);
        plugin.getDataStore().setFly(p.getUniqueId(), newVal);
        p.sendMessage(Component.text("Fly: " + (newVal ? "enabled" : "disabled")));
    }
}
