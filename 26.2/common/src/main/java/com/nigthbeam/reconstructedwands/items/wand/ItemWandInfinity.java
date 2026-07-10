package com.nigthbeam.reconstructedwands.items.wand;

import net.minecraft.world.item.ItemStack;

/**
 * Infinity wand with unlimited durability.
 */
public class ItemWandInfinity extends ItemWand {
    public ItemWandInfinity(Properties properties) {
        super(properties);
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isInfinityWand() {
        return true;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
