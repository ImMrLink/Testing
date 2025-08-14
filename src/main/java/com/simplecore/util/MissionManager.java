package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionManager {

    public static class Objective {
        public final String type; // e.g. break:DIAMOND_ORE or vote:any or afk_zone:fishing:minutes
        public final int amount;
        public Objective(String type, int amount) { this.type = type; this.amount = amount; }
    }

    public static class Reward {
        public final String type; // economy, command, item, kit, cratekey
        public final Map<String,Object> data;
        public Reward(String type, Map<String,Object> data) { this.type = type; this.data = data; }
    }

    public static class Mission {
        public static class Filters {
            public final java.util.Set<String> worldsAllow = new java.util.HashSet<>();
            public final java.util.Set<String> worldsDeny = new java.util.HashSet<>();
            public final java.util.Set<String> regionsDeny = new java.util.HashSet<>();
        }
public static class Mission {
        public final String id;
        public final String displayName;
        public final String scope; // daily, weekly, seasonal, onetime, repeatable
        public final String description;
        public final String permission; // optional
        public final String chain; public final int order; public final String next;
        public final boolean visible;
        public final String icon;
        public final List<Objective> objectives;
        public final List<Reward> rewards;
        public final boolean repeatable;
        public final int cooldownMinutes;
        public final Filters filters;
        public Mission(String id, String displayName, String scope, String description, String permission, String chain, int order, String next, boolean visible, String icon, List<Objective> objectives, List<Reward> rewards, boolean repeatable, int cooldownMinutes, Filters filters) {
            this.id=id; this.displayName=displayName; this.scope=scope; this.description=description; this.permission=permission;
            this.chain=chain; this.order=order; this.next=next; this.visible=visible; this.icon=icon; this.objectives=objectives; this.rewards=rewards; this.repeatable=repeatable; this.cooldownMinutes=cooldownMinutes; this.filters=filters;
        }
    }

    private final SimpleCorePlugin plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<String, Mission> missions = new LinkedHashMap<>();

    public MissionManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "missions.yml");
        if (!file.exists()) {
            plugin.saveResource("missions.yml", false);
        }
        reload();
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
        missions.clear();
        ConfigurationSection ms = cfg.getConfigurationSection("missions");
        if (ms != null) {
            for (String id : ms.getKeys(false)) {
                ConfigurationSection m = ms.getConfigurationSection(id);
                String dn = ChatColor.translateAlternateColorCodes('&', m.getString("display-name", id));
                String scope = m.getString("scope", "daily");
                String desc = m.getString("description", "");
                String perm = m.getString("permission", null);
                String chain = m.getString("chain", null);
                int order = m.getInt("order", 0);
                String next = m.getString("next", null);
                boolean vis = m.getBoolean("visible", true);
                String icon = m.getString("icon", "PAPER");
                List<Objective> objs = new ArrayList<>();
                for (Object o : m.getList("objectives", java.util.List.of())) {
                    if (o instanceof Map) {
                        Map<?,?> mm = (Map<?,?>) o;
                        String t = String.valueOf(mm.getOrDefault("type", "break:#any"));
                        int amt = Integer.parseInt(String.valueOf(mm.getOrDefault("amount", 1)));
                        objs.add(new Objective(t, amt));
                    }
                }
                List<Reward> rws = new ArrayList<>();
                boolean repeatable = m.getBoolean("repeatable", false);
                int cooldown = m.getInt("cooldown-minutes", 0);
                Filters filters = new Filters();
                if (m.isConfigurationSection("filters")) {
                    var f = m.getConfigurationSection("filters");
                    filters.worldsAllow.addAll(f.getStringList("worlds-allow"));
                    filters.worldsDeny.addAll(f.getStringList("worlds-deny"));
                    filters.regionsDeny.addAll(f.getStringList("regions-deny"));
                }
                for (Object o : m.getList("rewards", java.util.List.of())) {
                    if (o instanceof Map) {
                        Map<String,Object> mm = new HashMap<>();
                        ((Map<?,?>)o).forEach((k,v)-> mm.put(String.valueOf(k), v));
                        String t = String.valueOf(mm.getOrDefault("type", "economy"));
                        rws.add(new Reward(t, mm));
                    }
                }
                missions.put(id, new Mission(id, dn, scope, desc, perm, chain, order, next, vis, icon, objs, rws, repeatable, cooldown, filters));
            }
        }
    }

    public Collection<Mission> all() { return missions.values(); }
    public Mission get(String id) { return missions.get(id); }

    public boolean isUnlocked(Player p, Mission m) {
        if (m.permission != null && !m.permission.isEmpty() && !p.hasPermission(m.permission)) return false;
        if (m.chain != null && m.order > 0) {
            String current = plugin.getDataStoreString("users."+p.getUniqueId()+".missions.chains."+m.chain+".current", "");
            if (!current.isEmpty() && !m.id.equals(current)) return false;
        }
        return true;
    }

    public String scopeKey(Mission m) {
        if ("daily".equalsIgnoreCase(m.scope)) return LocalDate.now().toString();
        if ("weekly".equalsIgnoreCase(m.scope)) {
            var wf = WeekFields.ISO;
            LocalDate d = LocalDate.now();
            int week = d.get(wf.weekOfWeekBasedYear());
            return d.getYear() + "-W" + week;
        }
        if ("seasonal".equalsIgnoreCase(m.scope)) {
            return "season"; // simplified; could add seasonal id
        }
        return "default";
    }

    public long timeLeftMillis(Mission m) {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        if ("daily".equalsIgnoreCase(m.scope)) {
            java.time.LocalTime rt = java.time.LocalTime.parse(this.cfg.getString("settings.reset-times.daily", "00:00"));
            java.time.ZonedDateTime next = now.with(rt);
            if (!next.isAfter(now)) next = next.plusDays(1);
            return java.time.Duration.between(now, next).toMillis();
        }
        if ("weekly".equalsIgnoreCase(m.scope)) {
            String spec = this.cfg.getString("settings.reset-times.weekly", "MON 00:00");
            String[] parts = spec.split(" ");
            java.time.DayOfWeek dow = java.time.DayOfWeek.valueOf(parts[0]);
            java.time.LocalTime rt = java.time.LocalTime.parse(parts[1]);
            java.time.ZonedDateTime next = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(dow)).with(rt);
            if (!next.isAfter(now)) next = next.plusWeeks(1);
            return java.time.Duration.between(now, next).toMillis();
        }
        return -1;
    }
}
