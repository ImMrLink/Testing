package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CrateKeyListener implements Listener {
    private final SimpleCorePlugin plugin;
    public CrateKeyListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        var is = e.getItem();
        if (is == null || !is.hasItemMeta() || is.getItemMeta().getLocalizedName() == null) return;
        String tag = is.getItemMeta().getLocalizedName();
        if (!tag.startsWith("SC_KEY_") && !tag.startsWith("SC_VOUCHER_KEY_") && !tag.startsWith("SC_VOUCHER_KIT_")) return;
        e.setCancelled(true);
        var p = e.getPlayer();
        if (tag.startsWith("SC_KEY_")) {
            // opening requires crate name in offhand? We just can't know which crate - encode in tag
            String crate = tag.substring("SC_KEY_".length()).toLowerCase(java.util.Locale.ROOT);
            plugin.getCrateManager().open(p, crate, false);
            return;
        }
        if (tag.startsWith("SC_VOUCHER_KEY_")) {
            String data = tag.substring("SC_VOUCHER_KEY_".length());
            String[] parts = data.split(":");
            String crate = parts[0];
            int amt = Integer.parseInt(parts[1]);
            plugin.getCrateManager().addVirtualKeys(p.getUniqueId(), crate, amt);
            p.sendMessage(org.bukkit.ChatColor.GREEN + "Redeemed " + amt + " " + crate + " keys!");
            is.setAmount(is.getAmount()-1);
            p.getInventory().setItemInMainHand(is.getAmount()<=0?null:is);
            return;
        }
        if (tag.startsWith("SC_VOUCHER_KIT_")) {
            String kit = tag.substring("SC_VOUCHER_KIT_".length());
            plugin.getKitManager().giveIgnoringCooldown(p, kit);
            p.sendMessage(org.bukkit.ChatColor.GREEN + "Redeemed kit: " + kit);
            is.setAmount(is.getAmount()-1);
            p.getInventory().setItemInMainHand(is.getAmount()<=0?null:is);
        }
    }
}
