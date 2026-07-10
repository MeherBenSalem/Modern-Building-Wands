package com.nigthbeam.reconstructedwands.platform;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import com.nigthbeam.reconstructedwands.platform.services.INetworkHelper;
import com.nigthbeam.reconstructedwands.network.ModMessages;
import com.nigthbeam.reconstructedwands.network.PacketUndoBlocks;
import com.nigthbeam.reconstructedwands.wand.undo.UndoHistory;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Fabric implementation of INetworkHelper.
 * Note: Full networking requires packet registration - this is a stub for basic
 * functionality.
 */
public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof CustomPacketPayload payload) ModMessages.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        if (packet instanceof UndoHistory.UndoBlocksData data) {
            ModMessages.sendToPlayer(new PacketUndoBlocks(data.positions()), player);
        }
    }
}
