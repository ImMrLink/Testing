package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffModeCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public StaffModeCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("simplecore.staffmode")) { p.sendMessage(Component.text("No permission.")); return true; }
        var sm = plugin.getStaffModeManager();
        if (sm.isInStaff(p)) sm.exit(p); else sm.enter(p);
        return true;
    }
}
