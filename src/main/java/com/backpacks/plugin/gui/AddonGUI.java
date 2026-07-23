package com.backpacks.plugin.gui;

import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.BackpacksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AddonGUI {

    public static void openCrafting(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.WORKBENCH, "Backpack Crafting");
        player.openInventory(inv);
    }

    public static void openFurnace(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.FURNACE, "Backpack Furnace");
        player.openInventory(inv);
    }

    public static void openBlastFurnace(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.BLAST_FURNACE, "Backpack Blast Furnace");
        player.openInventory(inv);
    }

    public static void openSmoker(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.SMOKER, "Backpack Smoker");
        player.openInventory(inv);
    }

    public static void openFletchingTable(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, "Backpack Fletching Table");
        player.openInventory(inv);
    }

    public static void openGrindstone(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.GRINDSTONE, "Backpack Grindstone");
        player.openInventory(inv);
    }

    public static void openStonecutter(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.STONECUTTER, "Backpack Stonecutter");
        player.openInventory(inv);
    }

    public static void openSmithingTable(Player player) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.SMITHING, "Backpack Smithing Table");
        player.openInventory(inv);
    }

    public static void openJukebox(Player player, BackpackData data) {
        Inventory inv = Bukkit.createInventory(player, 9, "Backpack Jukebox");
        ItemStack discSlot = new ItemStack(Material.JUKEBOX);
        ItemMeta meta = discSlot.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§ePlace music disc here");
            meta.getPersistentDataContainer().set(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING, "jukebox");
            discSlot.setItemMeta(meta);
        }
        inv.setItem(4, discSlot);
        player.openInventory(inv);
    }

    public static void openQuiver(Player player, BackpackData data) {
        Inventory inv = Bukkit.createInventory(player, 9, "Backpack Quiver");
        player.openInventory(inv);
    }

    public static void openEnchant(Player player, BackpackData data) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.ENCHANTING, "Backpack Enchant");
        player.openInventory(inv);
    }
}
