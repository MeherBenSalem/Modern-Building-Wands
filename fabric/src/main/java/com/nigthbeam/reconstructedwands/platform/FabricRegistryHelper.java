package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.nigthbeam.reconstructedwands.platform.services.IRegistryHelper;

import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of IRegistryHelper.
 */
public class FabricRegistryHelper implements IRegistryHelper {
    @Override
    @Nullable
    public Item getItem(ResourceLocation location) {
        return BuiltInRegistries.ITEM.get(location).map(net.minecraft.core.Holder::value).orElse(null);
    }

    @Override
    @Nullable
    public ResourceLocation getItemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
