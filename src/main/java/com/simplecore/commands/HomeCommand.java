package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.DataStore;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public HomeCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        String name = args.length > 0 ? args[0] : "home";
        DataStore store = plugin.getDataStore();
        Location loc = store.getHome(p.getUniqueId(), name);
        if (loc == null) {
            p.sendMessage(Component.text("Home '" + name + "' not found. Use /sethome " + name));
            return true;
        }
        p.teleportAsync(loc).thenRun(() -> p.sendMessage(Component.text("Teleported to home '" + name + "'.")));
        return true;
    }
}
