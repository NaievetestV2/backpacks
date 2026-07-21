package com.backpacks.plugin.backpack;

import com.backpacks.plugin.BackpacksPlugin;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BackpackData {

    private final UUID id;
    private final BackpackTier tier;
    private final List<ItemStack> items;
    private final Set<String> addons;

    public BackpackData(UUID id, BackpackTier tier) {
        this.id = id;
        this.tier = tier;
        this.items = new ArrayList<>(Collections.nCopies(tier.slots(), ItemStack.empty()));
        this.addons = new HashSet<>();
    }

    public UUID id() { return id; }
    public BackpackTier tier() { return tier; }
    public List<ItemStack> items() { return items; }
    public Set<String> addons() { return addons; }

    public void addAddon(String addon) { addons.add(addon); }
    public boolean hasAddon(String addon) { return addons.contains(addon); }

    public static ItemStack createItem(BackpackTier tier) {
        ItemStack stack = new ItemStack(Material.CHEST);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + capitalize(tier.key()) + " Backpack");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey idKey = BackpacksPlugin.key("backpack_id");
            NamespacedKey tierKey = BackpacksPlugin.key("backpack_tier");
            UUID uuid = UUID.randomUUID();
            container.set(idKey, PersistentDataType.STRING, uuid.toString());
            container.set(tierKey, PersistentDataType.STRING, tier.key());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static BackpackTier readTier(ItemStack stack) {
        if (stack == null || stack.getType() != Material.CHEST || !stack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        String tier = meta.getPersistentDataContainer().get(BackpacksPlugin.key("backpack_tier"), PersistentDataType.STRING);
        return tier != null ? BackpackTier.fromKey(tier) : null;
    }

    public static UUID readId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        String id = stack.getItemMeta().getPersistentDataContainer().get(BackpacksPlugin.key("backpack_id"), PersistentDataType.STRING);
        return id != null ? UUID.fromString(id) : null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
