package com.backpacks.plugin;

import com.backpacks.plugin.backpack.BackpackManager;
import com.backpacks.plugin.command.BackpackCommand;
import com.backpacks.plugin.listener.BackpackListener;
import com.backpacks.plugin.recipe.BackpackRecipeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public final class BackpacksPlugin extends JavaPlugin {

    private static BackpacksPlugin instance;
    public static BackpacksPlugin getInstance() {
        return instance;
    }

    public static NamespacedKey key(String path) {
        return new NamespacedKey(getPlugin(BackpacksPlugin.class), path);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        File dataFolder = new File(getDataFolder(), "data");
        dataFolder.mkdirs();
        BackpackManager manager = new BackpackManager(dataFolder);
        BackpackRecipeManager recipeManager = new BackpackRecipeManager(this);
        getServer().getPluginManager().registerEvents(new BackpackListener(manager), this);
        BackpackCommand command = new BackpackCommand(manager);
        getCommand("backpack").setExecutor(command);
        getCommand("backpack").setTabCompleter(command);
        getLogger().info("Backpacks plugin enabled!");
        new BukkitRunnable() {
            @Override
            public void run() {
                recipeManager.register();
                getLogger().info("Recipes registered!");
            }
        }.runTaskLater(this, 20L);
    }

    @Override
    public void onDisable() {
        BackpackManager.getInstance().save();
        getLogger().info("Backpacks plugin disabled!");
    }

    public static void unlockRecipes(Player player) {
        player.discoverRecipe(key("backpack_copper"));
        player.discoverRecipe(key("backpack_iron"));
        player.discoverRecipe(key("backpack_gold"));
        player.discoverRecipe(key("backpack_diamond"));
        player.discoverRecipe(key("addon_crafting"));
        player.discoverRecipe(key("addon_jukebox"));
        player.discoverRecipe(key("addon_quiver"));
        player.discoverRecipe(key("addon_enchant"));
    }
}
