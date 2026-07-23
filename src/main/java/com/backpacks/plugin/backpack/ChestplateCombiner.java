package com.backpacks.plugin.backpack;

import com.backpacks.plugin.BackpacksPlugin;
import org.bukkit.Material;
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
        if (chest == null || chest.getType() != Material.LEATHER_CHESTPLATE) {
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
        if (backpack.getAmount() > 1) {
            backpack.setAmount(backpack.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        player.sendMessage("§aBackpack attached to chestplate");
        com.backpacks.plugin.backpack.BackpackManager.getInstance().save();
        return true;
    }

    public static boolean detach(Player player, UUID backpackId) {
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.LEATHER_CHESTPLATE) {
            player.sendMessage("§cYou must be wearing a leather chestplate");
            return false;
        }
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return false;
        List<String> parts = new ArrayList<>(List.of(raw.split(",")));
        String found = null;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).contains(backpackId.toString())) {
                found = parts.remove(i);
                break;
            }
        }
        if (found == null) {
            player.sendMessage("§cBackpack not found on chestplate");
            return false;
        }
        container.set(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING, String.join(",", parts));
        chest.setItemMeta(meta);
        for (BackpackData data : BackpackManager.getAllBackpacks()) {
            if (data.id().equals(backpackId)) {
                player.getInventory().addItem(BackpackData.createItem(data.tier()));
                break;
            }
        }
        player.sendMessage("§aBackpack detached from chestplate");
        com.backpacks.plugin.backpack.BackpackManager.getInstance().save();
        return true;
    }
}
