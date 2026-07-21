package com.backpacks.plugin.addon;

import org.bukkit.entity.Player;

public class QuiverAddon implements BackpackAddon {
    @Override
    public String type() { return "quiver"; }
    @Override
    public void open(Player player) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(player, 9, "Backpack Quiver");
        player.openInventory(inv);
    }
}
