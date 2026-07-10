package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.server.level.ServerPlayer;
import com.nigthbeam.reconstructedwands.platform.services.INetworkHelper;
import com.nigthbeam.reconstructedwands.network.ModMessages;
import com.nigthbeam.reconstructedwands.network.PacketUndoBlocks;
import com.nigthbeam.reconstructedwands.wand.undo.UndoHistory;

/**
 * Forge network helper for packet handling.
 */
public class ForgeNetworkHelper implements INetworkHelper {

    @Override
    public void sendToServer(Object packet) {
        ModMessages.sendToServer(packet);
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        if (packet instanceof UndoHistory.UndoBlocksData data) {
            ModMessages.sendToPlayer(new PacketUndoBlocks(data.positions()), player);
        }
    }
}
