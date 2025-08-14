package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DailyCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public DailyCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!sender.hasPermission("simplecore.daily")) { sender.sendMessage("No permission."); return true; }
        var mgr = plugin.getDailyRewardManager();
        if (!plugin.getConfig().getBoolean("daily-rewards.enabled", true)) { p.sendMessage(Component.text("Daily rewards are disabled.")); return true; }
        if (!mgr.canClaim(p)) {
            long accum = mgr.getAccumMs(p.getUniqueId());
            long reqMs = (long)(plugin.getConfig().getInt("daily-rewards.required-online-hours", 24) * 3600L * 1000L);
            long remain = Math.max(0, reqMs - accum);
            p.sendMessage(Component.text("Not ready. Time until 24h online claim: " + (remain / 3600000) + "h " + ((remain/60000)%60) + "m."));
            return true;
        }
        mgr.give(p);
        p.sendMessage(Component.text("Daily reward claimed."));
        return true;
    }
}
