package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class KitManager {
    public static final String PERM_GLOBAL_PREFIX = "simplecore.kit.cooldown.";
    public static final String PERM_PERKIT_PREFIX = "simplecore.kit.%s.cooldown.";
    private final SimpleCorePlugin plugin;

    public static class Kit {
        public final String name;
        public final int cooldownSeconds;
        public final List<ItemStack> items;
        public Kit(String name, int cooldownSeconds, List<ItemStack> items) {
            this.name = name;
            this.cooldownSeconds = cooldownSeconds;
            this.items = items;
        }
    }

    private final Map<String, Kit> kits = new HashMap<>();

    public KitManager(SimpleCorePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        kits.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("kits");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            int cd = sec.getInt("cooldown-seconds", 3600);
            java.util.List<?> raw = sec.getList("items");
            List<ItemStack> items = new ArrayList<>();
            if (raw != null) for (Object obj : raw) {
                // formats supported:
                // - MATERIAL:AMOUNT
                // - map:
                //     type: MATERIAL
                //     amount: N
                //     enchants: { sharpness: 2, unbreaking: 3 }
                if (obj instanceof String line) {
                String[] parts = line.split(":");
                if (parts.length >= 1) {
                    try {
                        Material m = Material.valueOf(parts[0].toUpperCase(Locale.ROOT));
                        int amt = parts.length >= 2 ? Integer.parseInt(parts[1]) : 1;
                        items.add(new ItemStack(m, Math.max(1, Math.min(64, amt))));
                    } catch (Exception ignored) {}
                }
            } else if (obj instanceof java.util.Map<?,?> map) {
                try {
                    Object t = map.get("type");
                    if (t == null) continue;
                    org.bukkit.Material m = org.bukkit.Material.valueOf(t.toString().toUpperCase(java.util.Locale.ROOT));
                    int amt = 1;
                    Object a = map.get("amount");
                    if (a != null) amt = Math.max(1, Math.min(64, Integer.parseInt(a.toString())));
                    ItemStack is = new ItemStack(m, amt);
                    Object ench = map.get("enchants");
                    if (ench instanceof java.util.Map<?,?> emap) {
                        ItemMeta meta = is.getItemMeta();
                        for (var entry : emap.entrySet()) {
                            String key = entry.getKey().toString().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
                            org.bukkit.enchantments.Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(key));
                            if (e == null) continue;
                            int lvl = Integer.parseInt(entry.getValue().toString());
                            meta.addEnchant(e, Math.max(1, lvl), true);
                        }
                        is.setItemMeta(meta);
                    }
                    items.add(is);
                } catch (Exception ignored) {}
            }
            kits.put(key.toLowerCase(Locale.ROOT), new Kit(key, cd, items));
        }
    }

    public Set<String> getKitNames() {
        return kits.keySet();
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean give(Player p, String kitName) {
        Kit kit = getKit(kitName);
        if (kit == null) return false;
        long now = System.currentTimeMillis();
        long last = getLastUse(p.getUniqueId(), kit.name);
        long remaining = last + kit.cooldownSeconds * 1000L - now;
        if (remaining > 0) {
            plugin.getLogWriter().logKit(p.getUniqueId(), kit.name, "denied", "cooldown=" + (remaining/1000) + "s remaining");
            p.sendMessage(Component.text("Kit cooldown: " + (remaining / 1000) + "s remaining."));
            return true;
        }
        // space check is skipped for simplicity (items will drop if full)
        for (ItemStack is : kit.items) {
            p.getInventory().addItem(is.clone());
        }
        setLastUse(p.getUniqueId(), kit.name, now);
        p.sendMessage(Component.text("Kit '" + kit.name + "' redeemed."));
        plugin.getMissionProgress().increment(p, "kit_claim:" + kit.name, 1);
        plugin.getLogWriter().logKit(p.getUniqueId(), kit.name, "claimed", "items=" + kit.items.size());
        return true;
    }

    private long getLastUse(UUID uuid, String kit) {
        return plugin.getDataStoreTime("users." + uuid + ".kits." + kit.toLowerCase(Locale.ROOT) + ".last", 0L);
    }
    private void setLastUse(UUID uuid, String kit, long ts) {
        plugin.setDataStoreTime("users." + uuid + ".kits." + kit.toLowerCase(Locale.ROOT) + ".last", ts);
    }

    public int effectiveCooldown(org.bukkit.entity.Player p, Kit kit) {
        int base = kit.cooldownSeconds;
        int best = base;
        // per-kit
        for (org.bukkit.permissions.PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase(java.util.Locale.ROOT);
            String perKit = String.format(PERM_PERKIT_PREFIX, kit.name.toLowerCase(java.util.Locale.ROOT));
            if (perm.startsWith(perKit)) {
                try { int secs = Integer.parseInt(perm.substring(perKit.length())); best = Math.min(best, Math.max(0, secs)); } catch (Exception ignored) {}
            }
        }
        // global
        for (org.bukkit.permissions.PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase(java.util.Locale.ROOT);
            if (perm.startsWith(PERM_GLOBAL_PREFIX)) {
                try { int secs = Integer.parseInt(perm.substring(PERM_GLOBAL_PREFIX.length())); best = Math.min(best, Math.max(0, secs)); } catch (Exception ignored) {}
            }
        }
        return best;
    }
    public long getRemaining(org.bukkit.entity.Player p, Kit kit) {
        long now = System.currentTimeMillis();
        long last = getLastUse(p.getUniqueId(), kit.name);
        int cd = effectiveCooldown(p, kit);
        long end = last + (long)cd * 1000L;
        return Math.max(0L, end - now);
    }


    public void giveIgnoringCooldown(org.bukkit.entity.Player p, String kitName) {
        var k = kits.get(kitName.toLowerCase(java.util.Locale.ROOT));
        if (k == null) { p.sendMessage(net.kyori.adventure.text.Component.text("Unknown kit.")); return; }
        giveItems(p, k);
        plugin.getLogWriter().logKit(p.getUniqueId(), k.name, "claimed_by_voucher", null);
    }

}
