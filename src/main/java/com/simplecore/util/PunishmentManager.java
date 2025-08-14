package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {
    private static final String ACTION_WARN = "WARN";
    private static final String ACTION_JAIL = "JAIL";
    private static final String ACTION_MUTE = "MUTE";
    private static final String ACTION_KICK = "KICK";
    private static final String ACTION_TEMPBAN = "TEMPBAN";
    private static final String ACTION_PERMBAN = "PERMBAN";
    private static final String ACTION_IPBAN = "IPBAN";
    private final SimpleCorePlugin plugin;

    // runtime caches
    private final Map<UUID, Long> muteUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> jailUntil = new ConcurrentHashMap<>();
    private Location jailLoc;
    private double jailRadius;

    public PunishmentManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // jail location from datastore
        this.jailLoc = plugin.getDataStore().getWarp("_jail"); // reuse warps store under a special name
        this.jailRadius = plugin.getConfig().getDouble("jail.radius", 5.0);
    }

    public void setJail(Location loc) {
        plugin.getDataStore().setWarp("_jail", loc);
        this.jailLoc = loc;
    }
    public Location getJail() { return jailLoc; }

    public void warn(Player staff, Player target, String reason) {
        target.sendMessage(net.kyori.adventure.text.Component.text("You have been warned." + (reason==null?"":" Reason: "+reason)));
        staff.sendMessage(net.kyori.adventure.text.Component.text("Warned " + target.getName()));
        plugin.getLogWriter().logPunishment(staff.getUniqueId(), target.getUniqueId(), ACTION_WARN, 0, reason, null);
    }

    // Mute
    public void mute(Player target, long millis) {
        plugin.getLogWriter().logPunishment(null, target.getUniqueId(), ACTION_MUTE, millis, null, null);
        long until = System.currentTimeMillis() + millis;
        muteUntil.put(target.getUniqueId(), until);
        target.sendMessage(Component.text("You have been muted."));
    }
    public boolean isMuted(UUID id) {
        Long until = muteUntil.get(id);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { muteUntil.remove(id); return false; }
        return true;
    }

    // Jail
    public void jail(Player target, long millis) {
        plugin.getLogWriter().logPunishment(null, target.getUniqueId(), ACTION_JAIL, millis, null, null);
        if (jailLoc == null) return;
        long until = System.currentTimeMillis() + millis;
        jailUntil.put(target.getUniqueId(), until);
        target.teleportAsync(jailLoc);
        target.sendMessage(Component.text("You have been jailed."));
    }
    public void unjail(UUID id) {
        jailUntil.remove(id);
    }
    public boolean isJailed(UUID id) {
        Long until = jailUntil.get(id);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { jailUntil.remove(id); return false; }
        return true;
    }
    public double jailRadius() { return jailRadius; }

    // Kick
    public void kick(Player target, String reason) {
        plugin.getLogWriter().logPunishment(null, target.getUniqueId(), ACTION_KICK, 0, reason, null);
        target.kick(Component.text(reason == null ? "Kicked." : reason));
    }

    // Temp ban
    public void tempBan(OfflinePlayer target, long millis, String reason) {
        plugin.getLogWriter().logPunishment(null, target.getUniqueId(), ACTION_TEMPBAN, millis, reason, null);
        Date until = new Date(System.currentTimeMillis() + millis);
        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, until, "SimpleCore");
        if (target.isOnline()) ((Player)target).kick(Component.text("Temp-banned: " + (reason == null ? "" : reason)));
    }

    // Perm ban
    public void permBan(OfflinePlayer target, String reason) {
        plugin.getLogWriter().logPunishment(null, target.getUniqueId(), ACTION_PERMBAN, 0, reason, null);
        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, "SimpleCore");
        if (target.isOnline()) ((Player)target).kick(Component.text("Banned: " + (reason == null ? "" : reason)));
    }

    // IP ban
    public void ipBan(String ip, String reason) {
        plugin.getLogWriter().logPunishment(null, null, ACTION_IPBAN, 0, reason, "ip="+ip);
        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, null, "SimpleCore");
    }
}
