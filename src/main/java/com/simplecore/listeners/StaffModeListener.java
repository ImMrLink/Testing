package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.StaffModeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class StaffModeListener implements Listener {
    private final SimpleCorePlugin plugin;
    public StaffModeListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        var sm = plugin.getStaffModeManager();
        Player p = e.getPlayer();
        if (!sm.isInStaff(p)) return;
        var tag = sm.tagOf(p.getInventory().getItemInMainHand());
        if (tag == null) return;

        switch (tag) {
            case StaffModeManager.TAG_FLY -> {
                if (!p.hasPermission("simplecore.staff.tool.fly")) { p.sendMessage(Component.text("No permission.")); return; }
                boolean newVal = !p.getAllowFlight();
                p.setAllowFlight(newVal || p.getGameMode() == GameMode.CREATIVE);
                p.setFlying(newVal);
                p.sendMessage(Component.text("Fly: " + (newVal ? "enabled" : "disabled")));
                e.setCancelled(true);
            }
            case StaffModeManager.TAG_SPEC -> {
                if (!p.hasPermission("simplecore.staff.tool.spec")) { p.sendMessage(Component.text("No permission.")); return; }
                if (p.getGameMode() == GameMode.SPECTATOR) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.sendMessage(Component.text("Spectator: disabled"));
                } else {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage(Component.text("Spectator: enabled"));
                }
                e.setCancelled(true);
            }
            default -> {}
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        var sm = plugin.getStaffModeManager();
        Player p = e.getPlayer();
        if (!sm.isInStaff(p)) return;
        var tag = sm.tagOf(p.getInventory().getItemInMainHand());
        if (tag == null) return;
        if (!(e.getRightClicked() instanceof Player target)) return;

        switch (tag) {
            case StaffModeManager.TAG_INVSEE -> {
                if (!p.hasPermission("simplecore.staff.tool.invsee")) { p.sendMessage(Component.text("No permission.")); return; }
                if (!p.hasPermission("simplecore.invsee")) { p.sendMessage(Component.text("No permission.")); return; }
                p.openInventory(target.getInventory());
                p.sendMessage(Component.text("Viewing " + target.getName() + "'s inventory."));
                e.setCancelled(true);
            }
            case StaffModeManager.TAG_PUNISH -> {
                if (!p.hasPermission("simplecore.staff.tool.punish")) { p.sendMessage(Component.text("No permission.")); return; }
                if (!p.hasPermission("simplecore.punish")) { p.sendMessage(Component.text("No permission.")); return; }
                plugin.getPunishmentGui().openRoot(p, target);
                e.setCancelled(true);
            }
            default -> {}
        }
    }
}
