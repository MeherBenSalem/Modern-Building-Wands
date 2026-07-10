package com.nigthbeam.reconstructedwands.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

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
    public static RecipeSerializer<RecipeWandUpgrade> createWandUpgradeSerializer() {
        return new RecipeSerializer<>(
                MapCodec.unit(RecipeWandUpgrade.INSTANCE),
                StreamCodec.<RegistryFriendlyByteBuf, RecipeWandUpgrade>unit(RecipeWandUpgrade.INSTANCE));
    }
}
