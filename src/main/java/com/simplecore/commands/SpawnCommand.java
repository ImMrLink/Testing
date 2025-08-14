package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.DataStore;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public SpawnCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        DataStore store = plugin.getDataStore();
        Location spawn = store.getSpawn();
        if (spawn == null) {
            World w = p.getWorld();
            spawn = w.getSpawnLocation();
        }
        p.teleportAsync(spawn).thenRun(() -> p.sendMessage(Component.text("Teleported to spawn.")));
        return true;
    }
}
