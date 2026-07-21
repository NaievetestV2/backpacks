package com.backpacks.plugin.addon;

import org.bukkit.entity.Player;

public class EnchantAddon implements BackpackAddon {
    @Override
    public String type() { return "enchant"; }
    @Override
    public void open(Player player) {
        player.openInventory(org.bukkit.Bukkit.createInventory(player, org.bukkit.event.inventory.InventoryType.ENCHANTING, "Backpack Enchant"));
    }
}
