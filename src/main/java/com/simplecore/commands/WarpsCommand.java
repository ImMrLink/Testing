package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class WarpsCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public WarpsCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Set<String> warps = plugin.getDataStore().listWarps();
        sender.sendMessage("Warps: " + (warps.isEmpty() ? "(none)" : String.join(", ", warps)));
        return true;
    }
}
