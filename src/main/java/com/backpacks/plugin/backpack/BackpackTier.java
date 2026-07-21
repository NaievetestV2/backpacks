package com.backpacks.plugin.backpack;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BackpackTier {

    COPPER("copper", 27, Color.fromRGB(0xE67E22), Material.COPPER_INGOT),
    IRON("iron", 32, Color.fromRGB(0x95A5A6), Material.IRON_INGOT),
    GOLD("gold", 40, Color.fromRGB(0xF1C40F), Material.GOLD_INGOT),
    DIAMOND("diamond", 45, Color.fromRGB(0x3498DB), Material.DIAMOND),
    NETHERITE("netherite", 74, Color.fromRGB(0x2C3E50), Material.NETHERITE_INGOT);

    private final String key;
    private final int slots;
    private final Color color;
    private final Material ingredient;

    BackpackTier(String key, int slots, Color color, Material ingredient) {
        this.key = key;
        this.slots = slots;
        this.color = color;
        this.ingredient = ingredient;
    }

    public String key() { return key; }
    public int slots() { return slots; }
    public Color color() { return color; }
    public Material ingredient() { return ingredient; }

    public static BackpackTier fromKey(String key) {
        for (BackpackTier tier : values()) {
            if (tier.key.equals(key)) return tier;
        }
        return null;
    }
}
