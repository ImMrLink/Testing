package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StaffModeManager {
    private final SimpleCorePlugin plugin;
    private final Map<UUID, ItemStack[]> savedInv = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<UUID, GameMode> savedMode = new HashMap<>();
    private final Map<UUID, Location> savedLoc = new HashMap<>();

    // Hotbar items
    public static final String TAG_FLY = "SC_STAFF_FLY";
    public static final String TAG_SPEC = "SC_STAFF_SPEC";
    public static final String TAG_INVSEE = "SC_STAFF_INVSEE";
    public static final String TAG_PUNISH = "SC_STAFF_PUNISH";

    public StaffModeManager(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public boolean isInStaff(Player p) { return savedInv.containsKey(p.getUniqueId()); }

    public void enter(Player p) {
        if (isInStaff(p)) return;
        savedInv.put(p.getUniqueId(), p.getInventory().getContents());
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());
        savedMode.put(p.getUniqueId(), p.getGameMode());
        savedLoc.put(p.getUniqueId(), p.getLocation());

        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setGameMode(GameMode.SURVIVAL);

        p.getInventory().setItem(0, make(Material.FEATHER, ChatColor.AQUA + "Toggle Fly", TAG_FLY));
        p.getInventory().setItem(1, make(Material.GLASS, ChatColor.GREEN + "Toggle Spectator (Vanish)", TAG_SPEC));
        p.getInventory().setItem(2, make(Material.ENDER_CHEST, ChatColor.YELLOW + "View Inventory", TAG_INVSEE));
        p.getInventory().setItem(3, make(Material.PAPER, ChatColor.RED + "Punish Player", TAG_PUNISH));
        p.sendMessage(Component.text("Entered staff mode."));
    }

    public void exit(Player p) {
        if (!isInStaff(p)) return;
        p.getInventory().setContents(savedInv.remove(p.getUniqueId()));
        p.getInventory().setArmorContents(savedArmor.remove(p.getUniqueId()));
        p.setGameMode(savedMode.remove(p.getUniqueId()));
        Location loc = savedLoc.remove(p.getUniqueId());
        if (loc != null) p.teleportAsync(loc);
        p.sendMessage(Component.text("Exited staff mode."));
    }

    private ItemStack make(Material mat, String name, String tag) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.values());
        // store tag in localized name (simple tag store)
        meta.setLocalizedName(tag);
        is.setItemMeta(meta);
        return is;
    }

    public String tagOf(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return null;
        return is.getItemMeta().getLocalizedName();
    }
}
