package com.simplecore.commands;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.AfkZoneManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class AfkZoneGuiCommand implements CommandExecutor {
    private final SimpleCorePlugin plugin;
    public AfkZoneGuiCommand(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        AfkZoneManager.Zone z = null;
        if (args.length >= 1) {
            for (var cand : plugin.getAfkZoneManager().listZones()) if (cand.name.equalsIgnoreCase(args[0])) { z = cand; break; }
        } else {
            z = plugin.getAfkZoneManager().findContaining(p.getLocation());
        }
        if (z == null) { p.sendMessage(Component.text("No AFK zone found.")); return true; }

        Inventory inv = Bukkit.createInventory(p, 27, "AFK Zone: " + z.name);
        // Build items from zone rewards if any, else show global info
        java.util.List<Map<String,Object>> rewards = z.rewards != null ? z.rewards : (java.util.List<Map<String,Object>>) plugin.getConfig().getList("AFK.rewards.in-zone");
        if (rewards == null) rewards = java.util.List.of();
        int slot = 10;
        for (var r : rewards) {
            String type = String.valueOf(r.getOrDefault("type","economy"));
            ItemStack it = new ItemStack(switch (type.toLowerCase()) {
                case "economy" -> Material.GOLD_INGOT;
                case "command" -> Material.COMMAND_BLOCK;
                default -> Material.PAPER;
            });
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName("§e" + type.toUpperCase());
            java.util.List<String> lore = new java.util.ArrayList<>();
            if (r.containsKey("amount-per-minute")) lore.add("§7Amount/min: §f" + r.get("amount-per-minute"));
            if (r.containsKey("command")) lore.add("§7Cmd: §f" + r.get("command"));
            meta.setLore(lore);
            it.setItemMeta(meta);
            inv.setItem(slot++, it);
            if (slot == 17) slot = 19;
            if (slot > 25) break;
        }
        // Multiplier indicator
        ItemStack mult = new ItemStack(Material.EMERALD);
        ItemMeta mm = mult.getItemMeta(); mm.setDisplayName("§aMultiplier: §fx" + z.multiplier); mult.setItemMeta(mm);
        inv.setItem(22, mult);
        p.openInventory(inv);
        return true;
    }
}
