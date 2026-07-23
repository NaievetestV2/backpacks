package com.backpacks.plugin.addon;

import com.backpacks.plugin.BackpacksPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AddonItemFactory {

    public static ItemStack create(String type) {
        Material mat = switch (type) {
            case "crafting" -> Material.CRAFTING_TABLE;
            case "jukebox" -> Material.JUKEBOX;
            case "quiver" -> Material.BOW;
            case "enchant" -> Material.ENCHANTING_TABLE;
            case "glider" -> Material.ELYTRA;
            default -> Material.PAPER;
        };
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b" + capitalize(type) + " Addon");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING, type);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static String readType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
