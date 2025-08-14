package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrateCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public CrateCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) { sender.sendMessage("/crate <open|preview|givekey|keys|genvoucher>"); return true; }
        switch (args[0].toLowerCase()) {
            case "open" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
                if (args.length < 2) { p.sendMessage(Component.text("Usage: /crate open <crate> [use-virtual]")); return true; }
                boolean useVirtual = args.length >= 3 && Boolean.parseBoolean(args[2]);
                plugin.getCrateManager().open(p, args[1], useVirtual);
            }
            case "preview" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
                if (args.length < 2) { p.sendMessage(Component.text("Usage: /crate preview <crate>")); return true; }
                plugin.getCrateManager().openPreview(p, args[1]);
            }
            case "givekey" -> {
                if (!sender.hasPermission("simplecore.crate.admin")) { sender.sendMessage("No permission."); return true; }
                if (args.length < 4) { sender.sendMessage("Usage: /crate givekey <player> <crate> <amount> [virtual|physical]"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                String crate = args[2];
                int amt = Integer.parseInt(args[3]);
                String mode = args.length>=5?args[4].toLowerCase():"physical";
                if ("virtual".equals(mode)) {
                    plugin.getCrateManager().addVirtualKeys(op.getUniqueId(), crate, amt);
                    sender.sendMessage(Component.text("Gave " + amt + " virtual keys to " + op.getName()));
                } else {
                    if (op.isOnline()) {
                        ((Player)op).getInventory().addItem(plugin.getCrateManager().makeKey(crate, amt));
                        sender.sendMessage(Component.text("Gave " + amt + " physical keys to " + op.getName()));
                    } else sender.sendMessage("Player must be online for physical keys.");
                }
            }
            case "keys" -> {
                if (sender instanceof Player p) {
                    if (args.length < 2) { p.sendMessage(Component.text("Usage: /crate keys <crate>")); return true; }
                    int n = plugin.getCrateManager().getVirtualKeys(p.getUniqueId(), args[1]);
                    p.sendMessage(Component.text("You have " + n + " " + args[1] + " keys."));
                } else sender.sendMessage("Players only.");
            }
            case "genvoucher" -> {
                if (!sender.hasPermission("simplecore.crate.admin")) { sender.sendMessage("No permission."); return true; }
                if (args.length < 4) { sender.sendMessage("Usage: /crate genvoucher <player> <crate> <amount>"); return true; }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) { sender.sendMessage("Player must be online."); return true; }
                var is = com.simplecore.util.VoucherUtil.makeKeyVoucher(plugin, args[2], Integer.parseInt(args[3]));
                p.getInventory().addItem(is);
                sender.sendMessage(Component.text("Voucher given."));
            }
            default -> sender.sendMessage("/crate <open|preview|givekey|keys|genvoucher>");
        }
        return true;
    }
}
