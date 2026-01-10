package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.platform.services.*;

import java.util.ServiceLoader;

/**
 * Service loader for platform-specific implementations.
 */
public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);
    public static final IConfigHelper CONFIG = load(IConfigHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);
    public static final IBlockEventHelper BLOCK_EVENTS = load(IBlockEventHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
