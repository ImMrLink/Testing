package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MissionsCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public MissionsCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            openMenu(p); return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> {
                var all = plugin.getMissionManager().all();
                for (var m : all) {
                    if (!plugin.getMissionManager().isUnlocked(p, m)) continue;
                    p.sendMessage(Component.text("- " + m.id + " " + ChatColor.stripColor(m.displayName)));
                }
                return true;
            }
            case "claim" -> {
                if (args.length < 2) { p.sendMessage(Component.text("Usage: /missions claim <id>")); return true; }
                var m = plugin.getMissionManager().get(args[1]);
                if (m == null) { p.sendMessage(Component.text("Unknown mission.")); return true; }
                plugin.getMissionProgress().claim(p, m);
                return true;
            }
            case "admin" -> {
                if (!p.hasPermission("simplecore.missions.admin.reload")) { p.sendMessage(Component.text("No permission.")); return true; }
                if (args.length==1 || args[1].equalsIgnoreCase("reload")) {
                    plugin.getMissionManager().reload();
                    p.sendMessage(Component.text("Missions reloaded."));
                    return true;
                }
                if (args[1].equalsIgnoreCase("give")) {
                    if (args.length<3) { p.sendMessage(Component.text("/missions admin give <player> <id>")); return true; }
                    var t = org.bukkit.Bukkit.getPlayer(args[2]);
                    var m = plugin.getMissionManager().get(args[3]);
                    if (t==null||m==null) { p.sendMessage(Component.text("Bad player or id.")); return true; }
                    // mark objectives complete
                    for (int i=0;i<m.objectives.size();i++) plugin.setDataStoreDouble("users."+t.getUniqueId()+".missions.progress."+m.id+".objectives."+i, m.objectives.get(i).amount);
                    p.sendMessage(Component.text("Granted progress."));
                    return true;
                }
                if (args[1].equalsIgnoreCase("reset")) {
                    if (args.length<4) { p.sendMessage(Component.text("/missions admin reset <player> <id>")); return true; }
                    var t = org.bukkit.Bukkit.getPlayer(args[2]);
                    var m = plugin.getMissionManager().get(args[3]);
                    if (t==null||m==null) { p.sendMessage(Component.text("Bad player or id.")); return true; }
                    for (int i=0;i<m.objectives.size();i++) plugin.setDataStoreDouble("users."+t.getUniqueId()+".missions.progress."+m.id+".objectives."+i, 0.0);
                    p.sendMessage(Component.text("Reset."));
                    return true;
                }
                if (args[1].equalsIgnoreCase("progress")) {
                    if (args.length<6) { p.sendMessage(Component.text("/missions admin progress <player> <id> <objIndex> <value|+delta>")); return true; }
                    var t = org.bukkit.Bukkit.getPlayer(args[2]);
                    var m = plugin.getMissionManager().get(args[3]);
                    int idx = Integer.parseInt(args[4]);
                    String val = args[5];
                    double current = plugin.getDataStoreDouble("users."+t.getUniqueId()+".missions.progress."+m.id+".objectives."+idx, 0.0);
                    if (val.startsWith("+")) current += Double.parseDouble(val.substring(1));
                    else current = Double.parseDouble(val);
                    plugin.setDataStoreDouble("users."+t.getUniqueId()+".missions.progress."+m.id+".objectives."+idx, current);
                    p.sendMessage(Component.text("Progress updated."));
                    return true;
                }
                p.sendMessage(Component.text("Usage: /missions admin <reload|give|reset|progress>"));
                return true; }
                if (!p.hasPermission("simplecore.missions.admin.reload")) { p.sendMessage(Component.text("No permission.")); return true; }
                plugin.getMissionManager().reload();
                p.sendMessage(Component.text("Missions reloaded."));
                return true;
            }
            default -> openMenu(p);
        }
        return true;
    }

    private void openMenu(Player p) {
        var inv = org.bukkit.Bukkit.createInventory(p, 54, "Missions");
        int slot = 0;
        for (var m : plugin.getMissionManager().all()) {
            if (!plugin.getMissionManager().isUnlocked(p, m)) continue;
            Material mat;
            try { mat = Material.valueOf(m.icon); } catch (Exception e) { mat = Material.PAPER; }
            ItemStack is = new ItemStack(mat);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(m.displayName);
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + m.description);
            boolean ready = plugin.getMissionProgress().isReady(p, m);
            lore.add(ready ? ChatColor.GREEN + "Ready to claim" : ChatColor.YELLOW + "In progress");
            im.setLore(lore);
            is.setItemMeta(im);
            inv.setItem(slot++, is);
            if (slot >= inv.getSize()) break;
        }
        p.openInventory(inv);
    }
}
