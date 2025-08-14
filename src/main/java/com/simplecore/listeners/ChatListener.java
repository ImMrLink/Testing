package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

// Minimal chat listener placeholder (can be extended for formats, nicknames, etc.)
public class ChatListener implements Listener {
    private final SimpleCorePlugin plugin;
    public ChatListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        // No-op by default; reserved for future formatting features.
    }
}
