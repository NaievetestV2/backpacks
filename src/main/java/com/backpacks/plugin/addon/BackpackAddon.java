package com.backpacks.plugin.addon;

import org.bukkit.entity.Player;

public interface BackpackAddon {
    String type();
    void open(Player player);
}
