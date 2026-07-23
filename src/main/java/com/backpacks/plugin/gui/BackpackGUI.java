package com.backpacks.plugin.gui;

import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.backpack.BackpackTier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BackpackGUI {

    private final Player player;
    private final BackpackData data;
    private int page;
    private Inventory openInventory;

    public BackpackGUI(Player player, BackpackData data) {
        this.player = player;
        this.data = data;
        this.page = 0;
    }

    public void open() {
        int totalRows = 6;
        int size = totalRows * 9;
        openInventory = Bukkit.createInventory(player, size, "Backpack - " + capitalize(data.tier().key()));
        render(openInventory);
        player.openInventory(openInventory);
    }

    public void render(Inventory inv) {
        List<ItemStack> items = data.items();
        int start = page * 45;
        int totalSlots = inv.getSize();
        for (int i = 0; i < totalSlots; i++) {
            int index = start + i;
            if (index < items.size()) {
                inv.setItem(i, items.get(index));
            } else if (i < 45) {
                inv.setItem(i, new ItemStack(Material.AIR));
            }
        }

        int addonStart = 5 * 9;
        if (data.hasAddon("crafting")) {
            inv.setItem(addonStart + 0, addonItem(Material.CRAFTING_TABLE, "Crafting"));
        }
        if (data.hasAddon("jukebox")) {
            inv.setItem(addonStart + 1, addonItem(Material.JUKEBOX, "Jukebox"));
        }
        if (data.hasAddon("quiver")) {
            inv.setItem(addonStart + 2, addonItem(Material.BOW, "Quiver"));
        }
        if (data.hasAddon("enchant")) {
            inv.setItem(addonStart + 3, addonItem(Material.ENCHANTING_TABLE, "Enchant"));
        }

        if (data.tier() == BackpackTier.NETHERITE) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§7Previous Page");
                prevMeta.getPersistentDataContainer().set(com.backpacks.plugin.BackpacksPlugin.key("gui_action"), PersistentDataType.STRING, "page_prev");
                prev.setItemMeta(prevMeta);
            }
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Next Page");
                nextMeta.getPersistentDataContainer().set(com.backpacks.plugin.BackpacksPlugin.key("gui_action"), PersistentDataType.STRING, "page_next");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(addonStart + 5, prev);
            inv.setItem(addonStart + 6, next);
        }

        ItemStack detach = new ItemStack(Material.BARRIER);
        ItemMeta detachMeta = detach.getItemMeta();
        if (detachMeta != null) {
            detachMeta.setDisplayName("§cDetach from Chestplate");
            detachMeta.getPersistentDataContainer().set(com.backpacks.plugin.BackpacksPlugin.key("gui_action"), PersistentDataType.STRING, "detach");
            detach.setItemMeta(detachMeta);
        }
        inv.setItem(addonStart + 8, detach);
    }

    public void saveContents() {
        if (openInventory == null) return;
        List<ItemStack> items = data.items();
        int start = page * 45;
        int totalSlots = openInventory.getSize();
        for (int i = 0; i < Math.min(totalSlots, 45); i++) {
            int index = start + i;
            if (index < items.size()) {
                items.set(index, openInventory.getItem(i));
            }
        }
        data.addons().clear();
        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                String addon = item.getItemMeta().getPersistentDataContainer().get(com.backpacks.plugin.BackpacksPlugin.key("addon_type"), org.bukkit.persistence.PersistentDataType.STRING);
                if (addon != null) {
                    data.addAddon(addon);
                }
            }
        }
    }

    public void nextPage() {
        if (data.tier() == BackpackTier.NETHERITE && page == 0) {
            int totalItems = data.items().size();
            if (totalItems > 45) {
                saveContents();
                page = 1;
                open();
            }
        }
    }

    public void prevPage() {
        if (data.tier() == BackpackTier.NETHERITE && page == 1) {
            saveContents();
            page = 0;
            open();
        }
    }

    private ItemStack addonItem(Material mat, String name) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b" + name);
            meta.getPersistentDataContainer().set(com.backpacks.plugin.BackpacksPlugin.key("addon_type"), PersistentDataType.STRING, name.toLowerCase());
            meta.getPersistentDataContainer().set(com.backpacks.plugin.BackpacksPlugin.key("gui_action"), PersistentDataType.STRING, "addon");
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
