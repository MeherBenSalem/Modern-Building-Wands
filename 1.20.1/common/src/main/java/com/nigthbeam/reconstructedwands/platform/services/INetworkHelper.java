package com.nigthbeam.reconstructedwands.platform.services;

import net.minecraft.server.level.ServerPlayer;

/**
 * Platform-specific networking operations.
 */
public interface INetworkHelper {
    /**
     * Send a packet to the server.
     */
    void sendToServer(Object packet);

    /**
     * Send a packet to a specific player.
     */
    void sendToPlayer(Object packet, ServerPlayer player);
}
