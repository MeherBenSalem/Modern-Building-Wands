package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge platform helper implementation.
 */
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public void openWandGUI(net.minecraft.world.item.ItemStack stack) {
        if (FMLEnvironment.dist.isClient()) {
            com.nigthbeam.reconstructedwands.client.ClientEvents.openWandGUI(stack);
        }
    }
}
