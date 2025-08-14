package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatListener implements Listener {
    private static final java.util.Map<Character, String> COLOR_PERMS = java.util.Map.ofEntries(
            java.util.Map.entry('0', "black"), java.util.Map.entry('1', "dark_blue"), java.util.Map.entry('2', "dark_green"),
            java.util.Map.entry('3', "dark_aqua"), java.util.Map.entry('4', "dark_red"), java.util.Map.entry('5', "dark_purple"),
            java.util.Map.entry('6', "gold"), java.util.Map.entry('7', "gray"), java.util.Map.entry('8', "dark_gray"), java.util.Map.entry('9', "blue"),
            java.util.Map.entry('a', "green"), java.util.Map.entry('b', "aqua"), java.util.Map.entry('c', "red"), java.util.Map.entry('d', "light_purple"),
            java.util.Map.entry('e', "yellow"), java.util.Map.entry('f', "white")
    );

    private final SimpleCorePlugin plugin;
    public ChatFormatListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        var gm = plugin.getGroupManager();
        String raw = e.getMessage();
        raw = handleRainbow(e.getPlayer(), raw);
        raw = applyColors(e.getPlayer(), raw);
        e.setMessage(raw);

        String fmt = plugin.getConfig().getString("chat.format", "&7[{prefix}] {name}&7: &f{message}");
        fmt = fmt.replace("{prefix}", gm.prefix(e.getPlayer()))
                 .replace("{suffix}", gm.suffix(e.getPlayer()))
                 .replace("{name}", e.getPlayer().getName())
                 .replace("{message}", "%2$s"); // message placeholder
        e.setFormat(ChatColor.translateAlternateColorCodes('&', fmt));
    }

    private String applyColors(org.bukkit.entity.Player p, String msg) {
        boolean wildcard = p.hasPermission("simplecore.chat.color.*");
        boolean basic = p.hasPermission("simplecore.chat.color.basic");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '&' && i + 1 < msg.length()) {
                char code = Character.toLowerCase(msg.charAt(i+1));
                if ("0123456789abcdeflmnokr".indexOf(code) >= 0) {
                    if (code >= '0' && code <= 'f') {
                        String name = COLOR_PERMS.get(code);
                        if (wildcard || p.hasPermission("simplecore.chat.color." + name) || basic) {
                            out.append(org.bukkit.ChatColor.COLOR_CHAR).append(code);
                        }
                    } else {
                        // formatting codes (bold, italic, etc.) allowed if basic
                        if (basic || wildcard) out.append(org.bukkit.ChatColor.COLOR_CHAR).append(code);
                    }
                    i++;
                    continue;
                }
            }
            // hex color &#RRGGBB
            if (msg.regionMatches(true, i, "&#", 0, 2) && i + 8 <= msg.length()) {
                String hex = msg.substring(i+2, i+8);
                if (plugin.getConfig().getBoolean("chat-colors.allow-hex", true) && hex.matches("[0-9a-fA-F]{6}") && (wildcard || p.hasPermission("simplecore.chat.color.hex"))) {
                    out.append(net.md_5.bungee.api.ChatColor.of("#" + hex));
                    i += 7;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private String handleRainbow(org.bukkit.entity.Player p, String msg) {
        String start = plugin.getConfig().getString("chat-colors.rainbow-tag", "[rainbow]"); 
        String end = plugin.getConfig().getString("chat-colors.rainbow-end", "[/rainbow]");
        if (!p.hasPermission("simplecore.chat.color.rainbow")) return msg;
        int s = msg.indexOf(start);
        int e = msg.indexOf(end);
        if (s >= 0 && e > s) {
            String pre = msg.substring(0, s);
            String mid = msg.substring(s + start.length(), e);
            String post = msg.substring(e + end.length());
            String colored = rainbow(mid);
            return pre + colored + post;
        }
        return msg;
    }

    private String rainbow(String text) {
        java.awt.Color[] colors = new java.awt.Color[] {
            new java.awt.Color(255,0,0), new java.awt.Color(255,127,0), new java.awt.Color(255,255,0),
            new java.awt.Color(0,255,0), new java.awt.Color(0,0,255),
            new java.awt.Color(75,0,130), new java.awt.Color(148,0,211)
        };
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (char ch : text.toCharArray()) {
            java.awt.Color c = colors[idx % colors.length];
            sb.append(net.md_5.bungee.api.ChatColor.of(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()))).append(ch);
            idx++;
        }
        return sb.toString();
    }

}
