package com.nigthbeam.reconstructedwands.items.wand;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import com.nigthbeam.reconstructedwands.platform.Services;

/**
 * Basic wand with durability based on tier.
 */
public class ItemWandBasic extends ItemWand {
    private final Tier tier;

    public ItemWandBasic(Properties properties, Tier tier) {
        super(properties.durability(tier.getUses()));
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

    public Tier getTier() {
        return tier;
    }
}
