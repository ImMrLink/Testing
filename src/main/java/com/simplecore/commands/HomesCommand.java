package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class HomesCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public HomesCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        Set<String> homes = plugin.getDataStore().listHomes(p.getUniqueId());
        p.sendMessage(Component.text("Homes: " + (homes.isEmpty() ? "(none)" : String.join(", ", homes))));
        return true;
    }
}
