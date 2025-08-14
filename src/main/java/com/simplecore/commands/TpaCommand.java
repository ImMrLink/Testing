package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpaCommand implements CommandExecutor {
    private static final Map<UUID, UUID> pending = new ConcurrentHashMap<>(); // target -> requester
    private final SimpleCorePlugin plugin;
    public TpaCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public static UUID getRequester(UUID target) { return pending.get(target); }
    public static void clear(UUID target) { pending.remove(target); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Component.text("Usage: /tpa <player>")); return true; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { p.sendMessage(Component.text("Player not found.")); return true; }
        if (target.getUniqueId().equals(p.getUniqueId())) { p.sendMessage(Component.text("You cannot TPA to yourself.")); return true; }

        pending.put(target.getUniqueId(), p.getUniqueId());
        int timeout = plugin.getConfig().getInt("teleport.tpa-timeout-seconds", 60);
        target.sendMessage(Component.text(p.getName() + " requested to teleport to you. /tpaccept or /tpdeny (" + timeout + "s)"));
        p.sendMessage(Component.text("TPA request sent to " + target.getName()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pending.get(target.getUniqueId()) != null && pending.get(target.getUniqueId()).equals(p.getUniqueId())) {
                pending.remove(target.getUniqueId());
                p.sendMessage(Component.text("TPA request to " + target.getName() + " expired."));
                target.sendMessage(Component.text("TPA request from " + p.getName() + " expired."));
            }
        }, timeout * 20L);
        return true;
    }
}
