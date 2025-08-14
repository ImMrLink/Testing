package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PunishmentGui {
    private final SimpleCorePlugin plugin;

    public enum Action { WARN, JAIL, MUTE, KICK, TEMPBAN, PERMBAN, IPBAN }

    public PunishmentGui(SimpleCorePlugin plugin) { this.plugin = plugin; }

    private ItemStack button(Material m, String name, String... loreLines) {
        ItemStack is = new ItemStack(m);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        List<String> lore = new ArrayList<>();
        for (String s : loreLines) lore.add(ChatColor.GRAY + s);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    public void openRoot(Player staff, Player target) {
        Inventory inv = Bukkit.createInventory(staff, 9, "Punish: " + target.getName());
        inv.setItem(0, button(Material.PAPER, "Warn"));
        inv.setItem(1, button(Material.IRON_BARS, "Jail"));
        inv.setItem(2, button(Material.BOOK, "Mute"));
        inv.setItem(3, button(Material.BARRIER, "Kick"));
        inv.setItem(4, button(Material.CLOCK, "Temp Ban"));
        inv.setItem(5, button(Material.ANVIL, "Perm Ban"));
        inv.setItem(6, button(Material.REDSTONE, "IP Ban"));
        staff.openInventory(inv);
        plugin.setPunishTarget(staff.getUniqueId(), target.getUniqueId());
    }

    public void openDurations(Player staff, String titlePrefix) {
        List<Integer> hours = plugin.getConfig().getIntegerList("punishments.durations-hours");
        int size = Math.min(54, ((hours.size() + 8) / 9) * 9);
        if (size == 0) size = 9;
        Inventory inv = Bukkit.createInventory(staff, size, titlePrefix + " Duration");
        int i = 0;
        for (int h : hours) {
            inv.setItem(i++, button(Material.CLOCK, h + " hours"));
        }
        staff.openInventory(inv);
    }
}
