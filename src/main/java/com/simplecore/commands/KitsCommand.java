package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.KitGui;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class KitsCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public KitsCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p && (args.length == 0 || !args[0].equalsIgnoreCase("-list"))) {
            new KitGui(plugin).open(p);
            return true;
        }
        var names = plugin.getKitManager().getKitNames();
        if (names.isEmpty()) {
            sender.sendMessage("No kits configured.");
        } else {
            sender.sendMessage("Kits: " + names.stream().sorted().collect(Collectors.joining(", ")));
        }
        return true;
    }
}
