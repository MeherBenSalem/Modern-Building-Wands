package com.nigthbeam.reconstructedwands.items.core;

import net.minecraft.resources.ResourceLocation;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.wand.action.ActionConstruction;

/**
 * Default wand core - standard construction action with no color tint.
 */
public class CoreDefault implements IWandCore {
    public static final CoreDefault INSTANCE = new CoreDefault();

    @Override
    public int getColor() {
        return -1;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionConstruction();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default");
    }
}
