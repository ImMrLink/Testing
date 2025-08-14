package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StaffToolsGuiListener implements Listener {
    private final SimpleCorePlugin plugin;
    public StaffToolsGuiListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!e.getView().getTitle().equals("Staff Tools")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
        switch (name.toLowerCase()) {
            case "toggle fly" -> {
                if (!p.hasPermission("simplecore.staff.tool.fly")) { p.sendMessage(Component.text("No permission.")); return; }
                boolean newVal = !p.getAllowFlight();
                p.setAllowFlight(newVal || p.getGameMode() == GameMode.CREATIVE);
                p.setFlying(newVal);
                p.sendMessage(Component.text("Fly: " + (newVal ? "enabled" : "disabled")));
            }
            case "toggle spectator" -> {
                if (!p.hasPermission("simplecore.staff.tool.spec")) { p.sendMessage(Component.text("No permission.")); return; }
                if (p.getGameMode() == GameMode.SPECTATOR) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.sendMessage(Component.text("Spectator: disabled"));
                } else {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage(Component.text("Spectator: enabled"));
                }
            }
            case "invsee" -> {
                if (!p.hasPermission("simplecore.staff.tool.invsee")) { p.sendMessage(Component.text("No permission.")); return; }
                p.sendMessage(Component.text("Tip: Right-click a player with the Invsee item."));
            }
            case "punish" -> {
                if (!p.hasPermission("simplecore.staff.tool.punish")) { p.sendMessage(Component.text("No permission.")); return; }
                p.sendMessage(Component.text("Tip: Right-click a player with the Punish item."));
            }
        }
    }
}
