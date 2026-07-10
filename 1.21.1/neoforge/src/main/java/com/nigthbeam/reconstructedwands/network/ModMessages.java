package com.nigthbeam.reconstructedwands.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import com.nigthbeam.reconstructedwands.Constants;

import javax.annotation.Nonnull;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        registrar.playToClient(PacketUndoBlocks.TYPE, PacketUndoBlocks.STREAM_CODEC, PacketUndoBlocks::handle);
        registrar.playToServer(PacketQueryUndo.TYPE, PacketQueryUndo.STREAM_CODEC, PacketQueryUndo::handle);
        registrar.playToServer(PacketWandOption.TYPE, PacketWandOption.STREAM_CODEC, PacketWandOption::handle);
    }

    public static void sendToServer(Object message) {
        if (message instanceof PacketQueryUndo packet)
            PacketDistributor.sendToServer(packet);
        else if (message instanceof PacketWandOption packet)
            PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        if (message instanceof PacketUndoBlocks packet)
            PacketDistributor.sendToPlayer(player, packet);
    }
}
