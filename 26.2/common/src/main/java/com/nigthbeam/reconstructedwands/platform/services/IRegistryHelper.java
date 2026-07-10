package com.nigthbeam.reconstructedwands.platform.services;

import net.minecraft.resources.Identifier;
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
    Item getItem(Identifier location);

    /**
     * Get the registry name of an item.
     */
    @Nullable
    Identifier getItemKey(Item item);
}
