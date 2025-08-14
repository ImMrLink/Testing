package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class CratePreviewAnimator {
    private final SimpleCorePlugin plugin;
    private final Random rng = new Random();

    public CratePreviewAnimator(SimpleCorePlugin plugin) { this.plugin = plugin; }

    public void openAnimated(Player p, String crate, List<ItemStack> pool, int durationTicks) {
        String title = "Preview: " + crate;
        Inventory inv = Bukkit.createInventory(p, 27, title);
        p.openInventory(inv);
        // fill borders with glass
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta(); fm.setDisplayName(" "); filler.setItemMeta(fm);
        int[] border = {0,1,2,3,4,5,6,7,8, 9,17, 18,19,20,21,22,23,24,25,26};
        for (int i: border) inv.setItem(i, filler);
        final int center = 13;

        final int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int ticks = 0;
            int step = 0;
            @Override public void run() {
                if (!p.isOnline() || !p.getOpenInventory().getTitle().equals(title)) { Bukkit.getScheduler().cancelTask(thisTask()); return; }
                ticks += 5;
                step++;
                // spin animation: pick random item from pool
                ItemStack show = pool.get(rng.nextInt(pool.size())).clone();
                inv.setItem(center, show);
                if (ticks >= durationTicks) {
                    Bukkit.getScheduler().cancelTask(thisTask());
                    // leave final item on display until player closes
                }
            }
            private int thisTask() { return task; }
        }, 0L, 5L);
    }
}
