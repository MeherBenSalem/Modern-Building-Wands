package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import com.nigthbeam.reconstructedwands.platform.services.IRegistryHelper;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge registry helper implementation.
 */
public class NeoForgeRegistryHelper implements IRegistryHelper {

    @Nullable
    @Override
    public Item getItem(ResourceLocation location) {
        return BuiltInRegistries.ITEM.get(location);
    }

    @Nullable
    @Override
    public ResourceLocation getItemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
