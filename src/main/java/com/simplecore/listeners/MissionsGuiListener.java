package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MissionsGuiListener implements Listener {
    private final SimpleCorePlugin plugin;
    public MissionsGuiListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle()==null) return;
        if (!e.getView().getTitle().equals("Missions") && !e.getView().getTitle().startsWith("Mission: ")) return;
        e.setCancelled(true);
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p)) return;
        ItemStack is = e.getCurrentItem();
        if (is==null || is.getType()==Material.AIR || is.getItemMeta()==null) return;

        if (e.getView().getTitle().equals("Missions")) {
            // open detail if display name matches a mission
            String name = ChatColor.stripColor(is.getItemMeta().getDisplayName());
            for (var m : plugin.getMissionManager().all()) {
                if (ChatColor.stripColor(m.displayName).equals(name)) {
                    openDetail(p, m.id);
                    return;
                }
            }
        } else {
            // detail view: last slot is claim button if ready
            String id = e.getView().getTitle().substring("Mission: ".length());
            var m = plugin.getMissionManager().get(id);
            if (m==null) return;
            if (e.getRawSlot()==e.getView().getTopInventory().getSize()-1) {
                plugin.getMissionProgress().claim(p, m);
                p.closeInventory();
            }
        }
    }

    private void openDetail(Player p, String id) {
        var m = plugin.getMissionManager().get(id);
        if (m==null) { p.sendMessage("Unknown mission."); return; }
        var inv = org.bukkit.Bukkit.createInventory(p, 27, "Mission: " + m.id);
        // info item
        var info = new org.bukkit.inventory.ItemStack(Material.BOOK);
        var im = info.getItemMeta();
        im.setDisplayName(m.displayName);
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.GRAY + m.description);
        long left = plugin.getMissionManager().timeLeftMillis(m);
        if (left>0) lore.add(ChatColor.DARK_GRAY + "Resets in: " + plugin.getMissionProgress().formatTimeLeft(left));
        im.setLore(lore);
        info.setItemMeta(im);
        inv.setItem(10, info);

        // progress item (bar)
        var prog = new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE);
        var pm = prog.getItemMeta();
        pm.setDisplayName(ChatColor.AQUA + "Progress");
        int have=0, need=0;
        for (int i=0;i<m.objectives.size();i++) {
            need += m.objectives.get(i).amount;
            have += (int) plugin.getDataStoreDouble("users."+p.getUniqueId()+".missions.progress."+m.id+".objectives."+i, 0.0);
        }
        int percent = need>0 ? (int)Math.min(100, Math.round(have*100.0/need)) : 100;
        String bar = ChatColor.GREEN + "█".repeat(percent/10) + ChatColor.DARK_GRAY + "█".repeat(10 - percent/10);
        pm.setLore(java.util.Arrays.asList(ChatColor.GRAY + (have + "/" + need), bar));
        prog.setItemMeta(pm);
        inv.setItem(13, prog);

        // claim button
        var claim = new org.bukkit.inventory.ItemStack(Material.LIME_DYE);
        var cm = claim.getItemMeta();
        boolean ready = plugin.getMissionProgress().isReady(p, m);
        cm.setDisplayName(ready ? ChatColor.GREEN + "Claim" : ChatColor.RED + "Not ready");
        claim.setItemMeta(cm);
        inv.setItem(26, claim);

        p.openInventory(inv);
    }
}
