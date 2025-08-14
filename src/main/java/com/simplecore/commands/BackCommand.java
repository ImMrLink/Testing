package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public BackCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        Location loc = plugin.getDataStore().getBack(p.getUniqueId());
        if (loc == null) { p.sendMessage(Component.text("No previous location stored.")); return true; }
        p.teleportAsync(loc).thenRun(() -> p.sendMessage(Component.text("Teleported back.")));
        return true;
    }
}
