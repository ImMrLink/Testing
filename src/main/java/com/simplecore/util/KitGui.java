package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KitGui {
    private final SimpleCorePlugin plugin;

    public KitGui(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        var km = plugin.getKitManager();
        var names = new java.util.ArrayList<>(km.getKitNames());
        names.sort(String::compareToIgnoreCase);
        int size = Math.max(9, ((names.size() + 8) / 9) * 9);
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ui.kits-gui-title", "&aKits"));
        Inventory inv = Bukkit.createInventory(p, Math.min(54, size), title);
        for (int i = 0; i < names.size() && i < size; i++) {
            var kit = km.getKit(names.get(i));
            if (kit == null) continue;
            Material icon = Material.CHEST;
            String iconPath = "kits." + kit.name + ".icon";
            String iconStr = plugin.getConfig().getString(iconPath, null);
            if (iconStr != null) {
                try { icon = Material.valueOf(iconStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
            } else if (!kit.items.isEmpty()) {
                icon = kit.items.get(0).getType();
            }
            ItemStack is = new ItemStack(icon);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + kit.name);
            List<String> lore = new ArrayList<>();
            long rem = km.getRemaining(p, kit);
            int cd = km.effectiveCooldown(p, kit);
            if (rem <= 0) {
                lore.add(ChatColor.GREEN + "Available now");
            } else {
                lore.add(ChatColor.RED + "Cooldown: " + (rem/1000) + "s");
            }
            lore.add(ChatColor.GRAY + "Cooldown base: " + cd + "s");
            meta.setLore(lore);
            is.setItemMeta(meta);
            inv.setItem(i, is);
        }
        p.openInventory(inv);
    }
}
