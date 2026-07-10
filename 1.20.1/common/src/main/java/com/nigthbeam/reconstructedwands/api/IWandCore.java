package com.nigthbeam.reconstructedwands.api;

import net.minecraft.resources.ResourceLocation;

/**
 * Interface for wand core upgrades that modify wand behavior.
 */
public interface IWandCore extends IWandUpgrade {
    /**
     * Get the registry name of this core.
     */
    ResourceLocation getRegistryName();

    /**
     * Get the wand action associated with this core.
     */
    IWandAction getWandAction();

    /**
     * Get the color tint for this core (-1 for no tint).
     */
    int getColor();
}
