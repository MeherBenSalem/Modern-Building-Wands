package com.nigthbeam.reconstructedwands.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

/**
 * Platform-specific registry operations.
 */
public interface IRegistryHelper {
    /**
     * Get an item by its registry name.
     */
    @Nullable
    Item getItem(ResourceLocation location);

    /**
     * Get the registry name of an item.
     */
    @Nullable
    ResourceLocation getItemKey(Item item);
}
