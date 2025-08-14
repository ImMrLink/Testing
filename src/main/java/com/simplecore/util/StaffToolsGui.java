package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StaffToolsGui {
    private final SimpleCorePlugin plugin;
    public StaffToolsGui(SimpleCorePlugin plugin) { this.plugin = plugin; }

    private ItemStack btn(Material m, String name, String lore) {
        ItemStack is = new ItemStack(m);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(java.util.List.of(ChatColor.GRAY + lore));
        is.setItemMeta(meta);
        return is;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, "Staff Tools");
        if (p.hasPermission("simplecore.staff.tool.fly"))
            inv.setItem(0, btn(Material.FEATHER, "Toggle Fly", "Enable/disable flight"));
        if (p.hasPermission("simplecore.staff.tool.spec"))
            inv.setItem(1, btn(Material.GLASS, "Toggle Spectator", "Become invisible (spectator)"));
        if (p.hasPermission("simplecore.staff.tool.invsee"))
            inv.setItem(2, btn(Material.ENDER_CHEST, "Invsee", "Right-click a player with Invsee item"));
        if (p.hasPermission("simplecore.staff.tool.punish"))
            inv.setItem(3, btn(Material.PAPER, "Punish", "Right-click a player to open punish GUI"));
        p.openInventory(inv);
    }
}
