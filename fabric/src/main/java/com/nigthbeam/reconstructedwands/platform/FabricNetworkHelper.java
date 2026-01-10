package com.nigthbeam.reconstructedwands.platform;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import com.nigthbeam.reconstructedwands.platform.services.INetworkHelper;

/**
 * Fabric implementation of INetworkHelper.
 * Note: Full networking requires packet registration - this is a stub for basic
 * functionality.
 */
public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void sendToServer(Object packet) {
        // Client-side networking handled in client module
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        // TODO: Implement proper packet sending when packet types are registered
    }
}
