package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class LogWriter {
    private final SimpleCorePlugin plugin;
    private final Path logDir;
    private volatile boolean enabled;

    public LogWriter(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("logs.enabled", true);
        String folder = plugin.getConfig().getString("logs.folder", "logs");
        this.logDir = plugin.getDataFolder().toPath().resolve(folder);
        try {
            Files.createDirectories(logDir);
        } catch (IOException ignored) {}
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("logs.enabled", true);
    }

    private Path daily(String prefix) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return logDir.resolve(prefix + "-" + date + ".log");
    }

    private synchronized void append(Path file, String line) {
        if (!enabled) return;
        try {
            Files.writeString(file, line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    private static String ts() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }

    private static String nameOf(UUID id) {
        if (id == null) return "-";
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        return (op != null && op.getName() != null) ? op.getName() : id.toString();
    }

    // ---- Public log APIs ----
    public void logKit(UUID player, String kitName, String status, String details) {
        String line = String.format("%s | player=%s | kit=%s | status=%s | %s",
                ts(), nameOf(player), kitName, status, details == null ? "" : details);
        append(daily("kits"), line);
    }

    public void logPunishment(UUID staff, UUID target, String action, long durationMillis, String reason, String extra) {
        String duration = durationMillis > 0 ? (durationMillis / 1000) + "s" : "-";
        String line = String.format("%s | staff=%s | target=%s | action=%s | duration=%s | reason=%s | %s",
                ts(), nameOf(staff), nameOf(target), action, duration, reason == null ? "-" : reason, extra == null ? "" : extra);
        append(daily("punishments"), line);
    }
}
