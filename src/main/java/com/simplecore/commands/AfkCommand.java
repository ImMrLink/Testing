package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.AfkManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public AfkCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        AfkManager mgr = plugin.getAfkManager();
        boolean afk = mgr.isAfk(p.getUniqueId());
        mgr.setAfk(p, !afk);
        mgr.markManual(p.getUniqueId(), !afk);
        if (!afk) p.sendMessage(Component.text("You are now AFK."));
        else p.sendMessage(Component.text("You are no longer AFK."));
        return true;
    }
}
