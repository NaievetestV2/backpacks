package com.backpacks.plugin.listener;

import com.backpacks.plugin.BackpacksPlugin;
import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.backpack.BackpackManager;
import com.backpacks.plugin.backpack.BackpackTier;
import com.backpacks.plugin.gui.AddonGUI;
import com.backpacks.plugin.gui.BackpackGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BackpackListener implements Listener {

    private final BackpackManager manager;
    private final Map<UUID, BackpackGUI> openGuis = new HashMap<>();

    public BackpackListener(BackpackManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            item = player.getInventory().getItemInMainHand();
        }
        if (item == null) return;

        String action = event.getAction().toString();

        if (action.contains("RIGHT_CLICK")) {
            if (isBackpack(item)) {
                if (canEquipDirectly(player)) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                    event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                    equipDirectly(player, item);
                    return;
                }
                event.setCancelled(true);
                event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                player.sendMessage("§aOpening backpack...");
                openBackpack(player, item);
            } else if (isAddonItem(item)) {
                event.setCancelled(true);
                event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                handleAddonItem(player, item);
            }
        } else if (action.contains("LEFT_CLICK")) {
            handleLeftClick(player, item, event);
        }
    }

    private boolean canEquipDirectly(Player player) {
        ItemStack chest = player.getInventory().getChestplate();
        return chest == null || chest.getType().isAir();
    }

    private void equipDirectly(Player player, ItemStack item) {
        UUID id = BackpackData.readId(item);
        if (id == null) {
            player.sendMessage("§cNo backpack data found");
            return;
        }
        BackpackData data = manager.getBackpack(id);
        if (data == null) {
            BackpackTier tier = BackpackData.readTier(item);
            if (tier == null) {
                player.sendMessage("§cBackpack tier not found");
                return;
            }
            data = new BackpackData(id, tier);
            manager.register(data);
        }
        ItemStack newBackpack = BackpackData.createItem(data.tier());
        UUID newId = BackpackData.readId(newBackpack);
        if (newId != null && newBackpack.getItemMeta() != null) {
            newBackpack.getItemMeta().getPersistentDataContainer().set(BackpacksPlugin.key("backpack_id"), PersistentDataType.STRING, id.toString());
        }
        player.getInventory().setChestplate(newBackpack);
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        player.sendMessage("§aBackpack equipped on chest!");
    }

    private void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {
        PlayerInventory inv = player.getInventory();
        ItemStack chest = inv.getChestplate();

        if (chest != null && chest.getType() == Material.LEATHER_CHESTPLATE && hasAttachedBackpacks(chest)) {
            if (isBackpack(item)) {
                event.setCancelled(true);
                event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                openChestplateSelection(player, chest);
                return;
            }
        }

        if (chest != null && chest.getType() == Material.LEATHER_CHESTPLATE && isBackpack(item)) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            com.backpacks.plugin.backpack.ChestplateCombiner.attach(player, item);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.LEATHER_CHESTPLATE) return;
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return;
        boolean hasGlider = false;
        for (String part : raw.split(",")) {
            if (part.isBlank()) continue;
            String[] split = part.split(":");
            if (split.length != 2) continue;
            UUID id = UUID.fromString(split[1]);
            BackpackData data = manager.getBackpack(id);
            if (data != null && data.hasAddon("glider")) {
                hasGlider = true;
                break;
            }
        }
        if (!hasGlider) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBowShoot(org.bukkit.event.entity.EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getInventory().contains(Material.ARROW)) return;
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.LEATHER_CHESTPLATE) return;
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return;
        for (String part : raw.split(",")) {
            if (part.isBlank()) continue;
            String[] split = part.split(":");
            if (split.length != 2) continue;
            UUID id = UUID.fromString(split[1]);
            BackpackData data = manager.getBackpack(id);
            if (data == null || !data.hasAddon("quiver")) continue;
            for (int i = 0; i < data.items().size(); i++) {
                ItemStack quiverItem = data.items().get(i);
                if (quiverItem != null && quiverItem.getType() == Material.ARROW) {
                    int amount = Math.min(quiverItem.getAmount(), 64);
                    ItemStack give = new ItemStack(Material.ARROW, amount);
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(give);
                    if (remaining.isEmpty()) {
                        quiverItem.setAmount(quiverItem.getAmount() - amount);
                        if (quiverItem.getAmount() <= 0) {
                            data.items().set(i, new ItemStack(Material.AIR));
                        }
                    }
                    break;
                }
            }
        }
    }

    private void openBackpack(Player player, ItemStack item) {
        UUID id = BackpackData.readId(item);
        if (id == null) {
            player.sendMessage("§cNo backpack data found on this item");
            return;
        }
        BackpackData data = manager.getBackpack(id);
        if (data == null) {
            BackpackTier tier = BackpackData.readTier(item);
            if (tier == null) {
                player.sendMessage("§cBackpack tier not found");
                return;
            }
            data = new BackpackData(id, tier);
            manager.register(data);
            player.sendMessage("§eBackpack data restored");
        }
        BackpackGUI gui = new BackpackGUI(player, data);
        openGuis.put(player.getUniqueId(), gui);
        gui.open();
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

    private boolean isAddonItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING);
    }

    private void handleAddonItem(Player player, ItemStack item) {
        String type = item.getItemMeta().getPersistentDataContainer().get(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING);
        if (type == null) return;
        switch (type) {
            case "crafting" -> AddonGUI.openCrafting(player);
            case "furnace" -> AddonGUI.openFurnace(player);
            case "blast_furnace" -> AddonGUI.openBlastFurnace(player);
            case "smoker" -> AddonGUI.openSmoker(player);
            case "fletching_table" -> AddonGUI.openFletchingTable(player);
            case "grindstone" -> AddonGUI.openGrindstone(player);
            case "stonecutter" -> AddonGUI.openStonecutter(player);
            case "smithing_table" -> AddonGUI.openSmithingTable(player);
            case "jukebox" -> AddonGUI.openJukebox(player, null);
            case "quiver" -> AddonGUI.openQuiver(player, null);
            case "enchant" -> AddonGUI.openEnchant(player, null);
            case "glider" -> {
                if (player.getInventory().contains(Material.ELYTRA) || player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                    player.sendMessage("§aGlider addon activated! Wear your elytra and equip a chestplate with this backpack to glide.");
                } else {
                    player.sendMessage("§cYou need an elytra to use the glider addon");
                }
            }
        }
    }

    private boolean hasAttachedBackpacks(ItemStack chest) {
        return !getAttachedBackpacks(chest).isEmpty();
    }

    private List<ItemStack> getAttachedBackpacks(ItemStack chest) {
        List<ItemStack> list = new ArrayList<>();
        ItemMeta chestMeta = chest.getItemMeta();
        if (chestMeta == null) return list;
        PersistentDataContainer container = chestMeta.getPersistentDataContainer();
        String raw = container.get(BackpacksPlugin.key("attached_backpacks"), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return list;
        for (String part : raw.split(",")) {
            if (part.isBlank()) continue;
            String[] split = part.split(":");
            if (split.length != 2) continue;
            UUID id = UUID.fromString(split[1]);
            BackpackData data = manager.getBackpack(id);
            if (data == null) continue;
            ItemStack backpack = BackpackData.createItem(data.tier());
            if (backpack.getItemMeta() != null) {
                backpack.getItemMeta().getPersistentDataContainer().set(BackpacksPlugin.key("backpack_id"), PersistentDataType.STRING, id.toString());
            }
            list.add(backpack);
        }
        return list;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
                        BackpackGUI gui = new BackpackGUI(player, data);
                        openGuis.put(player.getUniqueId(), gui);
                        gui.open();
                    }
                }
            }
        } else if (title.equals("Backpack Crafting")) {
            if (event.getSlot() == 0 && event.getCurrentItem() != null) {
                event.setCancelled(true);
                event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
                event.getInventory().setItem(0, null);
                event.getInventory().setItem(1, null);
                event.getInventory().setItem(2, null);
                event.getInventory().setItem(3, null);
                event.getInventory().setItem(4, null);
                event.getInventory().setItem(5, null);
                event.getInventory().setItem(6, null);
                event.getInventory().setItem(7, null);
                event.getInventory().setItem(8, null);
            }
        } else if (title.equals("Backpack Jukebox")) {
            Player who = (Player) event.getWhoClicked();
            if (event.getSlot() == 4) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().getType().toString().endsWith("_DISC")) {
                    String disc = event.getCurrentItem().getType().toString();
                    who.stopAllSounds();
                    who.playSound(who.getLocation(), org.bukkit.Sound.valueOf(disc), 1.0f, 1.0f);
                    who.sendMessage("§aNow playing: §e" + disc.replace("_", " ").toLowerCase());
                }
            }
        } else if (title.equals("Backpack Enchant")) {
            Player who = (Player) event.getWhoClicked();
            int bookshelves = 0;
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && item.getType() == Material.BOOKSHELF) {
                    bookshelves += item.getAmount();
                }
            }
            if (event.getSlot() == 0 && event.getCurrentItem() != null) {
                event.setCancelled(true);
                who.sendMessage("§aEnchanting with " + Math.min(bookshelves, 15) + " bookshelf power!");
                who.getInventory().addItem(event.getCurrentItem());
                event.getInventory().setItem(0, null);
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
            BackpackGUI gui = openGuis.get(player.getUniqueId());
            if (gui != null) gui.nextPage();
        } else if (action.equals("page_prev")) {
            BackpackGUI gui = openGuis.get(player.getUniqueId());
            if (gui != null) gui.prevPage();
        } else if (action.equals("detach")) {
            org.bukkit.inventory.Inventory backpackInv = event.getInventory();
            if (backpackInv != null && backpackInv.getSize() > 0) {
                ItemStack first = backpackInv.getItem(0);
                if (first != null) {
                    UUID id = BackpackData.readId(first);
                    if (id != null) {
                        com.backpacks.plugin.backpack.ChestplateCombiner.detach(player, id);
                    }
                }
            }
            player.closeInventory();
        } else if (action.equals("addon")) {
            String addon = container.get(BackpacksPlugin.key("addon_type"), PersistentDataType.STRING);
            if (addon != null) {
                switch (addon) {
                    case "crafting" -> AddonGUI.openCrafting(player);
                    case "furnace" -> AddonGUI.openFurnace(player);
                    case "blast_furnace" -> AddonGUI.openBlastFurnace(player);
                    case "smoker" -> AddonGUI.openSmoker(player);
                    case "fletching_table" -> AddonGUI.openFletchingTable(player);
                    case "grindstone" -> AddonGUI.openGrindstone(player);
                    case "stonecutter" -> AddonGUI.openStonecutter(player);
                    case "smithing_table" -> AddonGUI.openSmithingTable(player);
                    case "jukebox" -> AddonGUI.openJukebox(player, null);
                    case "quiver" -> AddonGUI.openQuiver(player, null);
                    case "enchant" -> AddonGUI.openEnchant(player, null);
                    case "glider" -> {
                        if (player.getInventory().contains(Material.ELYTRA) || player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                            player.sendMessage("§aGlider addon activated! Wear your elytra and equip a chestplate with this backpack to glide.");
                        } else {
                            player.sendMessage("§cYou need an elytra to use the glider addon");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        BackpackGUI gui = openGuis.remove(player.getUniqueId());
        if (gui != null) {
            gui.saveContents();
        }
    }
}
