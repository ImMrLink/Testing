package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitVoucherCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public KitVoucherCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("simplecore.kitvoucher.admin")) { sender.sendMessage("No permission."); return true; }
        if (args.length < 3 || !"give".equalsIgnoreCase(args[0])) { sender.sendMessage("Usage: /kitvoucher give <player> <kit>"); return true; }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) { sender.sendMessage("Player must be online."); return true; }
        String kit = args[2];
        p.getInventory().addItem(com.simplecore.util.VoucherUtil.makeKitVoucher(plugin, kit));
        sender.sendMessage(Component.text("Kit voucher given."));
        return true;
    }
}
