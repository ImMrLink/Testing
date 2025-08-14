package com.simplecore.economy;

import com.simplecore.SimpleCorePlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class EconomyService {
    private final SimpleCorePlugin plugin;
    private final double starting;
    private Economy vault;

    public EconomyService(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.starting = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
        // Try to hook Vault
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                var rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) vault = rsp.getProvider();
            }
        } catch (Throwable ignored) {}
    }

    public boolean usingVault() { return vault != null; }

    public double getBalance(UUID uuid) {
        if (vault != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            return vault.getBalance(op);
        }
        return plugin.getDataStore().getBalance(uuid, starting);
    }

    public void setBalance(UUID uuid, double amount) {
        if (vault != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            double cur = vault.getBalance(op);
            double delta = Math.max(0, amount) - cur;
            if (delta > 0) vault.depositPlayer(op, delta);
            else if (delta < 0) vault.withdrawPlayer(op, -delta);
            return;
        }
        plugin.getDataStore().setBalance(uuid, Math.max(0, amount));
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount < 0) return false;
        if (vault != null) {
            var r = vault.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
            return r.transactionSuccess();
        }
        setBalance(uuid, getBalance(uuid) + amount);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount < 0) return false;
        if (vault != null) {
            var r = vault.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
            return r.transactionSuccess();
        }
        double bal = getBalance(uuid);
        if (bal < amount) return false;
        setBalance(uuid, bal - amount);
        return true;
    }
}
