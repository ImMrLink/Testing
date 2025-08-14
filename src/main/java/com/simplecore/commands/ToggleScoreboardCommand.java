package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleScoreboardCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public ToggleScoreboardCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("simplecore.scoreboard.toggle")) { p.sendMessage(Component.text("No permission.")); return true; }
        String path = "users." + p.getUniqueId() + ".prefs.scoreboard";
        boolean cur = plugin.getDataStore().usersCfg.getBoolean(path, true);
        boolean next = !cur;
        plugin.getDataStore().usersCfg.set(path, next);
        plugin.getDataStore().saveAll();
        if (next) {
            plugin.getScoreboardManager().update(p);
            p.sendMessage(Component.text("Scoreboard: enabled."));
        } else {
            plugin.getScoreboardManager().disableFor(p);
            p.sendMessage(Component.text("Scoreboard: disabled."));
        }
        return true;
    }
}
