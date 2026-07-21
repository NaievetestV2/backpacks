package com.backpacks.plugin.addon;

import org.bukkit.entity.Player;

public class JukeboxAddon implements BackpackAddon {
    @Override
    public String type() { return "jukebox"; }
    @Override
    public void open(Player player) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(player, 9, "Backpack Jukebox");
        inv.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.JUKEBOX));
        player.openInventory(inv);
    }
}
