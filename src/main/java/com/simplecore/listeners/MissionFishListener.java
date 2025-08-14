package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class MissionFishListener implements Listener {
    private final SimpleCorePlugin plugin;
    public MissionFishListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getMissionProgress().increment(e.getPlayer(), "fish:any", 1);
        }
    }
}
