package com.backpacks.plugin.recipe;

import com.backpacks.plugin.BackpacksPlugin;
import com.backpacks.plugin.addon.AddonItemFactory;
import com.backpacks.plugin.backpack.BackpackData;
import com.backpacks.plugin.backpack.BackpackTier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

public class BackpackRecipeManager {

    private final BackpacksPlugin plugin;

    public BackpackRecipeManager(BackpacksPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        registerBackpack(BackpackTier.COPPER, Material.ORANGE_DYE);
        registerBackpack(BackpackTier.IRON, Material.LIGHT_GRAY_DYE);
        registerBackpack(BackpackTier.GOLD, Material.YELLOW_DYE);
        registerBackpack(BackpackTier.DIAMOND, Material.LIGHT_BLUE_DYE);
        registerBackpack(BackpackTier.NETHERITE, Material.BLACK_DYE);
        registerAddon("crafting", Material.CRAFTING_TABLE);
        registerAddon("jukebox", Material.JUKEBOX);
        registerAddon("quiver", Material.BOW);
        registerAddon("enchant", Material.ENCHANTING_TABLE);
    }

    private void registerBackpack(BackpackTier tier, Material dye) {
        NamespacedKey key = BackpacksPlugin.key("backpack_" + tier.key());
        ShapedRecipe recipe = new ShapedRecipe(key, BackpackData.createItem(tier));
        recipe.shape("LLL", "LCL", "LIL");
        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('I', tier.ingredient());
        recipe.setIngredient('D', dye);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerAddon(String name, Material base) {
        NamespacedKey key = BackpacksPlugin.key("addon_" + name);
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(key, AddonItemFactory.create(name), new RecipeChoice.ExactChoice(new ItemStack(base)), new RecipeChoice.MaterialChoice(Material.DIAMOND), null);
        plugin.getServer().addRecipe(recipe);
    }
}
