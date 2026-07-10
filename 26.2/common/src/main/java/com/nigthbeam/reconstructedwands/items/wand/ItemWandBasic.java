package com.nigthbeam.reconstructedwands.items.wand;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import com.nigthbeam.reconstructedwands.platform.Services;

/**
 * Basic wand with durability based on tier.
 */
public class ItemWandBasic extends ItemWand {
    private final ToolMaterial tier;

    public ItemWandBasic(Properties properties, ToolMaterial tier) {
        super(properties.durability(tier.durability()));
        this.tier = tier;
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    @Override
    public boolean isInfinityWand() {
        return false;
    }

    public ToolMaterial getTier() {
        return tier;
    }
}
