package com.nigthbeam.reconstructedwands.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import com.nigthbeam.reconstructedwands.Constants;

/**
 * Fabric client-side mod initializer.
 */
public class REConstructedWandsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Constants.LOG.info("Initializing {} client", Constants.MOD_NAME);

        // Register client events for wand mode switching
        ClientEvents.register();
        com.nigthbeam.reconstructedwands.network.ModMessages.registerClient();
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(RenderBlockPreview::onBlockOutline);
    }
}
