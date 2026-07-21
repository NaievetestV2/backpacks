package com.backpacks.plugin;

import com.backpacks.plugin.backpack.BackpackManager;
import com.backpacks.plugin.command.BackpackCommand;
import com.backpacks.plugin.listener.BackpackListener;
import com.backpacks.plugin.recipe.BackpackRecipeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackpacksPlugin extends JavaPlugin {

    public static NamespacedKey key(String path) {
        return new NamespacedKey(getPlugin(BackpacksPlugin.class), path);
    }

    @Override
    public void onEnable() {
        BackpackManager manager = new BackpackManager();
        BackpackRecipeManager recipeManager = new BackpackRecipeManager(this);
        recipeManager.register();
        getServer().getPluginManager().registerEvents(new BackpackListener(manager), this);
        BackpackCommand command = new BackpackCommand(manager);
        getCommand("backpack").setExecutor(command);
        getCommand("backpack").setTabCompleter(command);
    }
}
