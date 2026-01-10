package com.nigthbeam.reconstructedwands.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

/**
 * Holds references to custom recipe serializers.
 * Registration is handled by platform-specific code.
 */
public class ModRecipes {
    public static RecipeSerializer<RecipeWandUpgrade> WAND_UPGRADE_SERIALIZER;

    /**
     * Called by platform-specific code after registration to set the serializer
     * reference.
     */
    public static void setWandUpgradeSerializer(RecipeSerializer<RecipeWandUpgrade> serializer) {
        WAND_UPGRADE_SERIALIZER = serializer;
    }

    /**
     * Creates a new wand upgrade serializer instance.
     */
    public static SimpleCraftingRecipeSerializer<RecipeWandUpgrade> createWandUpgradeSerializer() {
        return new SimpleCraftingRecipeSerializer<>(RecipeWandUpgrade::new);
    }
}
