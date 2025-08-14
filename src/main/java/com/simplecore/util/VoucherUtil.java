package com.simplecore.util;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class VoucherUtil {
    public static ItemStack makeKeyVoucher(SimpleCorePlugin plugin, String crate, int amount) {
        ItemStack is = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aKey Voucher"));
        meta.setLore(java.util.List.of(ChatColor.GRAY + "Right-click to redeem " + amount + " " + crate + " keys"));
        meta.setLocalizedName("SC_VOUCHER_KEY_" + crate.toLowerCase(java.util.Locale.ROOT) + ":" + amount);
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack makeKitVoucher(SimpleCorePlugin plugin, String kit) {
        var mat = Material.matchMaterial(plugin.getConfig().getString("kit-vouchers.item.material", "PAPER"));
        ItemStack is = new ItemStack(mat==null?Material.PAPER:mat, 1);
        ItemMeta meta = is.getItemMeta();
        String name = plugin.getConfig().getString("kit-vouchers.item.name", "&aKit Voucher");
        java.util.List<String> lore = plugin.getConfig().getStringList("kit-vouchers.item.lore");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.replace("{kit}", kit)));
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String s : lore) out.add(ChatColor.translateAlternateColorCodes('&', s.replace("{kit}", kit)));
        meta.setLore(out);
        meta.setLocalizedName("SC_VOUCHER_KIT_" + kit.toLowerCase(java.util.Locale.ROOT));
        is.setItemMeta(meta);
        return is;
    }
}
