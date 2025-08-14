package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.StaffToolsGui;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffToolsCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public StaffToolsCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("simplecore.staff.tools")) { p.sendMessage(Component.text("No permission.")); return true; }
        new StaffToolsGui(plugin).open(p);
        return true;
    }
}
