package com.backpacks.plugin.listener;

import com.backpacks.plugin.BackpacksPlugin;
import com.backpacks.plugin.addon.AddonItemFactory;
import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.backpack.BackpackManager;
import com.backpacks.plugin.backpack.BackpackTier;
import com.backpacks.plugin.gui.AddonGUI;
import com.backpacks.plugin.gui.BackpackGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BackpackListener implements Listener {

    private final BackpackManager manager;

    public BackpackListener(BackpackManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        if (event.getAction().toString().contains("RIGHT")) {
            if (isBackpack(item)) {
                event.setCancelled(true);
                openBackpack(player, item);
            }
        } else if (event.getAction().toString().contains("LEFT")) {
            PlayerInventory inv = player.getInventory();
            ItemStack chest = inv.getChestplate();
            if (chest != null && chest.getType() == Material.LEATHER_CHESTPLATE && hasAttachedBackpacks(chest)) {
                event.setCancelled(true);
                if (player.isSneaking()) {
                    openChestplateSelection(player, chest);
                }
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;
        if (player.isInsideVehicle()) return;
        PlayerInventory inv = player.getInventory();
        ItemStack chest = inv.getChestplate();
        if (chest != null && chest.getType() == Material.LEATHER_CHESTPLATE && hasAttachedBackpacks(chest)) {
            openChestplateSelection(player, chest);
        }
    }

    private void openBackpack(Player player, ItemStack item) {
        UUID id = BackpackData.readId(item);
        if (id == null) return;
        BackpackData data = manager.getBackpack(id);
        if (data == null) return;
        new BackpackGUI(player, data).open();
    }

    private void openChestplateSelection(Player player, ItemStack chest) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(player, 9, "Select Backpack");
        List<ItemStack> attached = getAttachedBackpacks(chest);
        for (int i = 0; i < Math.min(attached.size(), 9); i++) {
            inv.setItem(i, attached.get(i));
        }
        player.openInventory(inv);
    }

    private boolean isBackpack(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(BackpacksPlugin.key("backpack_id"), PersistentDataType.STRING);
    }

    private boolean hasAttachedBackpacks(ItemStack chest) {
        return !getAttachedBackpacks(chest).isEmpty();
    }

    private List<ItemStack> getAttachedBackpacks(ItemStack chest) {
        List<ItemStack> list = new ArrayList<>();
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return list;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return list;
        for (String part : raw.split(",")) {
            if (part.isBlank()) continue;
            String[] split = part.split(":");
            if (split.length != 2) continue;
            Material mat = Material.matchMaterial(split[0]);
            if (mat == null) continue;
            ItemStack stack = new ItemStack(mat);
            ItemMeta stackMeta = stack.getItemMeta();
            if (stackMeta != null) {
                stackMeta.getPersistentDataContainer().set(BackpacksPlugin.key("backpack_id"), PersistentDataType.STRING, split[1]);
                stack.setItemMeta(stackMeta);
            }
            list.add(stack);
        }
        return list;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("Backpack - ")) {
            handleBackpackClick(event);
        } else if (title.equals("Select Backpack")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && isBackpack(event.getCurrentItem())) {
                Player player = (Player) event.getWhoClicked();
                player.closeInventory();
                UUID id = BackpackData.readId(event.getCurrentItem());
                if (id != null) {
                    BackpackData data = manager.getBackpack(id);
                    if (data != null) {
                        new BackpackGUI(player, data).open();
                    }
                }
            }
        } else if (title.equals("Backpack Crafting")) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                event.setCancelled(true);
                event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
            }
        } else if (title.equals("Backpack Jukebox")) {
            if (event.getSlot() == 4) {
                event.setCancelled(true);
            }
        }
    }

    private void handleBackpackClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String action = container.get(BackpacksPlugin.key("gui_action"), PersistentDataType.STRING);
        if (action == null) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (action.equals("page_next")) {
            new BackpackGUI(player, manager.getBackpack(BackpackData.readId(event.getClickedInventory().getItem(event.getSlot())))).nextPage();
        } else if (action.equals("page_prev")) {
            new BackpackGUI(player, manager.getBackpack(BackpackData.readId(event.getClickedInventory().getItem(event.getSlot())))).prevPage();
        } else if (action.equals("detach")) {
            player.closeInventory();
        }

        String addon = container.get(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING);
        if (addon != null) {
            switch (addon) {
                case "crafting" -> AddonGUI.openCrafting(player);
                case "jukebox" -> AddonGUI.openJukebox(player, null);
                case "quiver" -> AddonGUI.openQuiver(player, null);
                case "enchant" -> AddonGUI.openEnchant(player, null);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
    }
}
