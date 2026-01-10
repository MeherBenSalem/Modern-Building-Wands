package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.server.level.ServerPlayer;
import com.nigthbeam.reconstructedwands.platform.services.INetworkHelper;

/**
 * NeoForge network helper for packet handling.
 * TODO: Implement NeoForge networking for client-server communication.
 */
public class NeoForgeNetworkHelper implements INetworkHelper {

    @Override
    public void sendToServer(Object packet) {
        // TODO: NeoForge networking implementation
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        // TODO: NeoForge networking implementation
    }
}
