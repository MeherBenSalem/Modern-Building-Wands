package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.platform.services.IPlatformHelper;
import net.neoforged.fml.loading.FMLLoader;

/**
 * NeoForge platform helper implementation.
 */
public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}
