package com.backpacks.plugin.addon;

import org.bukkit.entity.Player;

public class CraftingAddon implements BackpackAddon {
    @Override
    public String type() { return "crafting"; }
    @Override
    public void open(Player player) {
        player.openInventory(org.bukkit.Bukkit.createInventory(player, org.bukkit.event.inventory.InventoryType.WORKBENCH, "Backpack Crafting"));
    }
}
