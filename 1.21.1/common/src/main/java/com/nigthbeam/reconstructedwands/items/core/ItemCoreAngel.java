package com.nigthbeam.reconstructedwands.items.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.wand.action.ActionAngel;

/**
 * Angel core - allows placing blocks behind target or in mid-air.
 */
public class ItemCoreAngel extends Item implements IWandCore {
    public ItemCoreAngel(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0x00BFFF; // Sky blue
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionAngel();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "core_angel");
    }
}
