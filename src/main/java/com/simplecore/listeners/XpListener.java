package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class XpListener implements Listener {
    private final SimpleCorePlugin plugin;
    public XpListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onExp(PlayerExpChangeEvent e) {
        int before = e.getAmount();
        int scaled = plugin.getMultiplierManager().scaledExp(e.getPlayer().getUniqueId(), before);
        e.setAmount(scaled);
    }
}
