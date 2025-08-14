package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private final SimpleCorePlugin plugin;
    public static class Group {
        public final java.util.List<String> inherits;
        public final String name;
        public final int priority;
        public final String prefix;
        public final String suffix;
        public final List<String> permissions;
        public Group(String name, int priority, String prefix, String suffix, java.util.List<String> perms, java.util.List<String> inherits) {
            this.name = name; this.priority = priority; this.prefix = prefix; this.suffix = suffix; this.permissions = perms; this.inherits = inherits == null ? java.util.List.of() : inherits;
        }
    }

    private final Map<String, Group> groups = new HashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    public GroupManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        groups.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("groups");
        if (sec == null) return;
        for (String g : sec.getKeys(false)) {
            ConfigurationSection gs = sec.getConfigurationSection(g);
            int pr = gs.getInt("priority", 0);
            String prefix = gs.getString("prefix", "");
            String suffix = gs.getString("suffix", "");
            List<String> perms = gs.getStringList("permissions");
            java.util.List<String> inh = gs.getStringList("inherits");
            groups.put(g.toLowerCase(Locale.ROOT), new Group(g, pr, prefix, suffix, perms, inh));
        }
        // Re-apply for online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            apply(p);
        }
    }

    public Set<String> getGroupNames() { return groups.keySet(); }

    public Group getGroup(String name) { return name == null ? null : groups.get(name.toLowerCase(Locale.ROOT)); }

    public String getPlayerGroupName(UUID uuid) {
        String key = "player-groups." + uuid.toString();
        String byUuid = plugin.getConfig().getString(key, null);
        if (byUuid != null) return byUuid;
        // also allow name key for convenience
        String byName = plugin.getConfig().getString("player-groups." + Bukkit.getOfflinePlayer(uuid).getName(), null);
        return byName;
    }

    public void setPlayerGroup(UUID uuid, String group) {
        plugin.getConfig().set("player-groups." + uuid.toString(), group);
        plugin.saveConfig();
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) apply(p);
    }

    public Group getPlayerGroup(UUID uuid) {
        String name = getPlayerGroupName(uuid);
        if (name != null) return getGroup(name);
        return getGroup("default");
    }

    public void apply(Player p) {
        // remove existing attachment
        PermissionAttachment old = attachments.remove(p.getUniqueId());
        if (old != null) p.removeAttachment(old);

        Group g = getPlayerGroup(p.getUniqueId());
        if (g == null) g = getGroup("default");
        PermissionAttachment att = p.addAttachment(plugin);
        attachments.put(p.getUniqueId(), att);

        // Basic wildcard support for '*' under simplecore.* (shallow)
        for (String perm : g.permissions) {
            if (perm.endsWith(".*")) {
                String base = perm.substring(0, perm.length() - 2);
                // allow base.* and the base itself
                att.setPermission(base, true);
                att.setPermission(base + ".*", true);
            } else {
                att.setPermission(perm, true);
            }
        }
    }

    public String prefix(Player p) {
        Group g = getPlayerGroup(p.getUniqueId());
        String text = (g != null ? g.prefix : "");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String suffix(Player p) {
        Group g = getPlayerGroup(p.getUniqueId());
        String text = (g != null ? g.suffix : "");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private java.util.Set<String> resolvePerms(Group g, java.util.Set<String> seen) {
        if (g == null) return java.util.Set.of();
        if (seen.contains(g.name.toLowerCase(java.util.Locale.ROOT))) return java.util.Set.of();
        seen.add(g.name.toLowerCase(java.util.Locale.ROOT));
        java.util.Set<String> out = new java.util.HashSet<>(g.permissions);
        if (g.inherits != null) {
            for (String parent : g.inherits) {
                Group pg = getGroup(parent);
                out.addAll(resolvePerms(pg, seen));
            }
        }
        return out;
    }

}
