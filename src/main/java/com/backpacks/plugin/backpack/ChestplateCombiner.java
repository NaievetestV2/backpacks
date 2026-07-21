package com.backpacks.plugin.backpack;

import com.backpacks.plugin.BackpacksPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestplateCombiner {

    public static boolean attach(Player player, ItemStack backpack) {
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != org.bukkit.Material.LEATHER_CHESTPLATE) {
            player.sendMessage("§cYou must be wearing a leather chestplate");
            return false;
        }
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        List<String> parts = raw == null || raw.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(raw.split(",")));
        if (parts.size() >= 6) {
            player.sendMessage("§cChestplate can only hold 6 backpacks");
            return false;
        }
        UUID id = BackpackData.readId(backpack);
        if (id == null) return false;
        parts.add(backpack.getType() + ":" + id);
        container.set(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING, String.join(",", parts));
        chest.setItemMeta(meta);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage("§aBackpack attached to chestplate");
        return true;
    }

    public static boolean toggleGlider(Player player) {
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != org.bukkit.Material.LEATHER_CHESTPLATE) {
            player.sendMessage("§cYou must be wearing a leather chestplate");
            return false;
        }
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean current = container.get(BackpacksPlugin.key("glider"), PersistentDataType.BYTE) != null && container.get(BackpacksPlugin.key("glider"), PersistentDataType.BYTE) == 1;
        container.set(BackpacksPlugin.key("glider"), PersistentDataType.BYTE, (byte) (current ? 0 : 1));
        chest.setItemMeta(meta);
        player.sendMessage(current ? "§cGlider disabled" : "§aGlider enabled");
        return true;
    }
}
