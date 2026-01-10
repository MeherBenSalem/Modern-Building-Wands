package com.nigthbeam.reconstructedwands;

import com.nigthbeam.reconstructedwands.basics.ReplacementRegistry;
import com.nigthbeam.reconstructedwands.crafting.ModRecipes;
import com.nigthbeam.reconstructedwands.crafting.RecipeWandUpgrade;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

/**
 * Fabric mod entrypoint.
 */
public class REConstructedWandsFabric implements ModInitializer {

    // Recipe serializers
    public static RecipeSerializer<RecipeWandUpgrade> WAND_UPGRADE_SERIALIZER;

    @Override
    public void onInitialize() {
        // Initialize common code
        REConstructedWands.init();

        // Initialize networking
        com.nigthbeam.reconstructedwands.network.ModMessages.register();

        // Register items
        ModItems.register();

        // Register recipe serializers
        WAND_UPGRADE_SERIALIZER = Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wand_upgrade"),
                ModRecipes.createWandUpgradeSerializer());
        ModRecipes.setWandUpgradeSerializer(WAND_UPGRADE_SERIALIZER);

        // Register server events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ReplacementRegistry.init();
            Constants.LOG.info("REConstructedWands server starting");
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            REConstructedWands.instance.undoHistory.removePlayer(handler.getPlayer());
        });

        Constants.LOG.info("REConstructedWands Fabric initialized");
    }
}
