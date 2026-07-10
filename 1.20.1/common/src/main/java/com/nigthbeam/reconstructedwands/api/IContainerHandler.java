package com.nigthbeam.reconstructedwands.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for handling container items (bundles, shulker boxes, backpacks,
 * etc.)
 * that can provide blocks for the wand.
 */
public interface IContainerHandler {
    /**
     * Check if this handler can handle the given inventory stack.
     */
    boolean matches(Player player, ItemStack target, ItemStack inventoryStack);

    /**
     * Count how many of the target item are available in the container.
     */
    int countItems(Player player, ItemStack target, ItemStack inventoryStack);

    /**
     * Take items from the container. Returns the number of items that could NOT be
     * taken.
     */
    int useItems(Player player, ItemStack target, ItemStack inventoryStack, int count);
}
