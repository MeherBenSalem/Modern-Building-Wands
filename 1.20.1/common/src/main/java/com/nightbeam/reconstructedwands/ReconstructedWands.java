package com.nightbeam.reconstructedwands;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.platform.Platform;
import com.nightbeam.reconstructedwands.platform.Registrar;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconstructedWands {
    public static final String MOD_ID = "reconstructedwands";
    public static final String MOD_NAME = "RE:Constructed Wands";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static Platform platform;
    private static boolean initialized;

    private ReconstructedWands() {
    }

    /** Called once by each loader's entry point, before registries freeze. */
    public static void init(Platform loaderPlatform, Registrar registrar) {
        if (initialized) {
            return;
        }
        initialized = true;
        platform = loaderPlatform;
        WandsConfig.load(platform.configDir());
        ModContent.register(registrar);
    }

    public static Platform platform() {
        return platform;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
