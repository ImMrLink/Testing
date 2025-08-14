package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CrateManager {
    private final SimpleCorePlugin plugin;
    private final File file;
    private FileConfiguration cfg;

    public CrateManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
        reload();
    }

    public void reload() {
        try {
            if (!file.exists()) { file.getParentFile().mkdirs(); plugin.saveResource("crates.yml", false); }
        } catch (Exception ignored) {}
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public Set<String> listCrates() {
        ConfigurationSection sec = cfg.getConfigurationSection("crates");
        if (sec == null) return java.util.Set.of();
        return sec.getKeys(false);
    }

    public boolean hasVirtualKey(UUID id, String crate) {
        double v = plugin.getDataStoreDouble("users."+id+".keys."+crate, 0);
        return v >= 1.0;
    }

    public void giveVirtualKey(UUID id, String crate, int amount) {
        double v = plugin.getDataStoreDouble("users."+id+".keys."+crate, 0);
        plugin.setDataStoreDouble("users."+id+".keys."+crate, v + amount);
    }

    public boolean consumeVirtualKey(UUID id, String crate) {
        double v = plugin.getDataStoreDouble("users."+id+".keys."+crate, 0);
        if (v < 1) return false;
        plugin.setDataStoreDouble("users."+id+".keys."+crate, v - 1);
        return true;
    }

    public ItemStack makePhysicalKey(String crate) {
        String path = "crates."+crate+".key.";
        Material mat = Material.matchMaterial(cfg.getString(path+"item", "TRIPWIRE_HOOK"));
        ItemStack is = new ItemStack(mat == null ? Material.TRIPWIRE_HOOK : mat);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', cfg.getString(path+"name", "&aCrate Key")));
        java.util.List<String> lore = cfg.getStringList(path+"lore");
        if (lore != null && !lore.isEmpty()) {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (String s : lore) out.add(ChatColor.translateAlternateColorCodes('&', s));
            meta.setLore(out);
        }
        meta.setLocalizedName("SC_KEY_" + crate.toLowerCase(java.util.Locale.ROOT));
        is.setItemMeta(meta);
        return is;
    }

    public boolean isPhysicalKey(ItemStack is, String crate) {
        if (is == null || !is.hasItemMeta()) return false;
        String loc = is.getItemMeta().getLocalizedName();
        return ("SC_KEY_" + crate.toLowerCase(java.util.Locale.ROOT)).equalsIgnoreCase(loc);
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> pickReward(String crate) {
        String base = "crates."+crate+".rewards";
        java.util.List<Map<String,Object>> list = (java.util.List<Map<String,Object>>) cfg.getList(base);
        if (list == null || list.isEmpty()) return null;
        // if any has 'percent', roll by percent
        boolean hasPercent = false;
        for (var m : list) if (m.containsKey("percent")) { hasPercent = true; break; }
        double r = ThreadLocalRandom.current().nextDouble(100.0);
        if (hasPercent) {
            double cum = 0.0;
            for (var m : list) {
                double p = Double.parseDouble(String.valueOf(m.getOrDefault("percent", 0)));
                cum += p;
                if (r < cum) return m;
            }
            return list.get(list.size()-1);
        } else {
            // weight based
            double total = 0.0;
            for (var m : list) total += Double.parseDouble(String.valueOf(m.getOrDefault("weight", 1)));
            double w = ThreadLocalRandom.current().nextDouble(total);
            double acc = 0.0;
            for (var m : list) {
                acc += Double.parseDouble(String.valueOf(m.getOrDefault("weight", 1)));
                if (w <= acc) return m;
            }
            return list.get(list.size()-1);
        }
    }

    public boolean open(Player p, String crate, boolean consumeVirtualIfNoPhysical) {
        String keyType = cfg.getString("crates."+crate+".key.type", "both").toLowerCase(java.util.Locale.ROOT);
        boolean needPhysical = keyType.equals("physical");
        boolean needVirtual = keyType.equals("virtual");
        boolean both = keyType.equals("both");

        // try physical first if allowed
        if (!needVirtual) {
            for (int i = 0; i < p.getInventory().getSize(); i++) {
                var is = p.getInventory().getItem(i);
                if (isPhysicalKey(is, crate)) {
                    // consume 1
                    if (is.getAmount() <= 1) p.getInventory().setItem(i, null); else is.setAmount(is.getAmount()-1);
                    return payout(p, crate);
                }
            }
            if (needPhysical) { plugin.getMissionProgress().increment(p, "crate_open:" + crateId, 1);
        p.sendMessage(ChatColor.RED + "You need a physical key."); return false; }
        }
        // else try virtual
        if (!needPhysical) {
            if (consumeVirtualKey(p.getUniqueId(), crate)) {
                return payout(p, crate);
            }
            if (needVirtual || consumeVirtualIfNoPhysical) {
                plugin.getMissionProgress().increment(p, "crate_open:" + crateId, 1);
        p.sendMessage(ChatColor.RED + "You need a virtual key.");
                return false;
            }
        }
        plugin.getMissionProgress().increment(p, "crate_open:" + crateId, 1);
        p.sendMessage(ChatColor.RED + "No suitable key found.");
        return false;
    }

    private boolean payout(Player p, String crate) {
        var reward = pickReward(crate);
        if (reward == null) { plugin.getMissionProgress().increment(p, "crate_open:" + crateId, 1);
        p.sendMessage(ChatColor.RED + "No rewards configured."); return false; }
        String type = String.valueOf(reward.getOrDefault("type", "economy")).toLowerCase(java.util.Locale.ROOT);
        switch (type) {
            case "economy" -> {
                double amount = reward.get("amount")==null?0.0:Double.parseDouble(reward.get("amount").toString());
                if (amount > 0) plugin.getEconomy().deposit(p.getUniqueId(), plugin.getMultiplierManager().scaledMoney(p.getUniqueId(), amount));
            }
            case "command" -> {
                String cmd = String.valueOf(reward.getOrDefault("command", ""));
                if (!cmd.isEmpty()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", p.getName()));
            }
            case "item" -> {
                String mat = String.valueOf(reward.getOrDefault("material", "STONE"));
                int amt = Integer.parseInt(String.valueOf(reward.getOrDefault("amount", 1)));
                var m = org.bukkit.Material.matchMaterial(mat);
                if (m != null) p.getInventory().addItem(new ItemStack(m, amt));
            }
        }
        plugin.getMissionProgress().increment(p, "crate_open:" + crateId, 1);
        p.sendMessage(ChatColor.GREEN + "You opened " + crate + "!");
        return true;
    }
}
