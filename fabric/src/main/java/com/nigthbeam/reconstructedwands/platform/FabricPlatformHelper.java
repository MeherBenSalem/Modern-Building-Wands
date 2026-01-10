package com.nigthbeam.reconstructedwands.platform;

import net.fabricmc.loader.api.FabricLoader;
import com.nigthbeam.reconstructedwands.platform.services.IPlatformHelper;

/**
 * Fabric implementation of IPlatformHelper.
 */
public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
