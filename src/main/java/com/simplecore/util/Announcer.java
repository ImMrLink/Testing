package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Announcer {
    private final SimpleCorePlugin plugin;
    private final Random rng = new Random();
    private int taskId = -1;

    public Announcer(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("announcer.enabled", true)) return;
        scheduleNext();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void scheduleNext() {
        int min = Math.max(5, plugin.getConfig().getInt("announcer.min-seconds", 120));
        int max = Math.max(min, plugin.getConfig().getInt("announcer.max-seconds", 300));
        int delay = (min == max) ? min : (min + rng.nextInt(max - min + 1));
        taskId = Bukkit.getScheduler().runTaskLater(plugin, this::broadcastRandom, delay * 20L).getTaskId();
    }

    private void broadcastRandom() {
        List<String> msgs = plugin.getConfig().getStringList("announcer.messages");
        if (msgs == null || msgs.isEmpty()) { scheduleNext(); return; }
        String raw = msgs.get(rng.nextInt(msgs.size()));
        Component msg = parseMessage(raw);
        for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(msg);
        scheduleNext();
    }

    private static final Pattern URL = Pattern.compile("(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);

    private Component parseMessage(String raw) {
        // translate & codes
        String colored = ChatColor.translateAlternateColorCodes('&', raw);
        // split by URL and wrap with click events
        java.util.List<Component> parts = new java.util.ArrayList<>();
        Matcher m = URL.matcher(colored);
        int last = 0;
        while (m.find()) {
            String before = colored.substring(last, m.start());
            if (!before.isEmpty()) parts.add(Component.text(before));
            String url = m.group(1);
            parts.add(Component.text(url).clickEvent(ClickEvent.openUrl(url)));
            last = m.end();
        }
        if (last < colored.length()) parts.add(Component.text(colored.substring(last)));
        Component out = Component.empty();
        for (Component c : parts) out = out.append(c);
        return out;
    }
}
