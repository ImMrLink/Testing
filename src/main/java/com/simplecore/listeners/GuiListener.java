package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import com.simplecore.util.KitGui;
import com.simplecore.util.KitManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {
    private final SimpleCorePlugin plugin;
    public GuiListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p)) return;
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ui.kits-gui-title", "&aKits"));
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        var meta = e.getCurrentItem().getItemMeta();
        if (meta.getDisplayName() == null) return;
        String name = ChatColor.stripColor(meta.getDisplayName());
        KitManager km = plugin.getKitManager();
        if (km.getKit(name.toLowerCase()) == null) return;
        if (!p.hasPermission("simplecore.kit." + name.toLowerCase()) && !p.isOp()) {
            p.sendMessage(Component.text("No permission: simplecore.kit." + name.toLowerCase()));
            return;
        }
        long rem = km.getRemaining(p, km.getKit(name.toLowerCase()));
        if (rem > 0) {
            p.sendMessage(Component.text("Cooldown remaining: " + (rem/1000) + "s"));
            return;
        }
        km.give(p, name);
        // refresh GUI after claim
        new KitGui(plugin).open(p);
    }
}
