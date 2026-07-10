package com.nigthbeam.reconstructedwands.items.core;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.wand.action.ActionDestruction;

/**
 * Destruction core - allows bulk block removal.
 */
public class ItemCoreDestruction extends Item implements IWandCore {
    public ItemCoreDestruction(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0xFF4500; // Orange red
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionDestruction();
    }

    @Override
    public Identifier getRegistryName() {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "core_destruction");
    }
}
