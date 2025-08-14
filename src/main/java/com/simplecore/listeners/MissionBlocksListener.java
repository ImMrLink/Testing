package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MissionBlocksListener implements Listener {
    private final SimpleCorePlugin plugin;
    public MissionBlocksListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler public void onBreak(BlockBreakEvent e) {
        String mat = e.getBlock().getType().name();
        plugin.getMissionProgress().increment(e.getPlayer(), "break:"+mat, 1);
        if (mat.endsWith("_ORE")) plugin.getMissionProgress().increment(e.getPlayer(), "ore:"+mat, 1);
        plugin.getMissionProgress().increment(e.getPlayer(), "break:#any", 1);
    }

    @EventHandler public void onPlace(BlockPlaceEvent e) {
        String mat = e.getBlock().getType().name();
        plugin.getMissionProgress().increment(e.getPlayer(), "place:"+mat, 1);
    }
}
