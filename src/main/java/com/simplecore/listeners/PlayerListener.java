package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.DataStore;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    private final SimpleCorePlugin plugin;
    private final DataStore store;

    public PlayerListener(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getDataStore();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // Apply saved flags
        boolean god = store.isGod(p.getUniqueId());
        boolean fly = store.canFly(p.getUniqueId());
        p.setInvulnerable(god);
        p.setAllowFlight(fly || p.getGameMode() == GameMode.CREATIVE);
        p.setFlying(fly && p.getGameMode() != GameMode.SURVIVAL ? p.isFlying() : p.isFlying()); // leave as-is
        p.sendMessage(Component.text("Welcome to the server, " + p.getName() + "!"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // nothing persisted here beyond DataStore.saveAll in onDisable
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getEntity() instanceof Player p) {
            store.setBack(p.getUniqueId(), p.getLocation());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getPlayer() != null && e.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) {
            store.setBack(e.getPlayer().getUniqueId(), e.getFrom());
        }
    }
}
