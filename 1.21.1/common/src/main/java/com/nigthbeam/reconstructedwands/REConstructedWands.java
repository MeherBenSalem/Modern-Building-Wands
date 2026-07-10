package com.nigthbeam.reconstructedwands;

import com.nigthbeam.reconstructedwands.containers.ContainerManager;
import com.nigthbeam.reconstructedwands.wand.undo.UndoHistory;
import net.minecraft.resources.ResourceLocation;

/**
 * Main mod class singleton for common module.
 */
public class REConstructedWands {
    public static REConstructedWands instance;

    public ContainerManager containerManager;
    public UndoHistory undoHistory;

    public REConstructedWands() {
        instance = this;
        containerManager = new ContainerManager();
        undoHistory = new UndoHistory();
    }

    /**
     * Common initialization called by all loaders.
     */
    public static void init() {
        if (instance == null) {
            instance = new REConstructedWands();
        }
        Constants.LOG.info("REConstructedWands initialized on {}",
                com.nigthbeam.reconstructedwands.platform.Services.PLATFORM.getPlatformName());
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
