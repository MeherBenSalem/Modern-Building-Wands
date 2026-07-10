package com.nigthbeam.reconstructedwands.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import com.nigthbeam.reconstructedwands.Constants;
import net.minecraft.resources.ResourceLocation;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketUndoBlocks.class, PacketUndoBlocks::encode, PacketUndoBlocks::decode,
                PacketUndoBlocks::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, PacketQueryUndo.class, PacketQueryUndo::encode, PacketQueryUndo::decode,
                PacketQueryUndo::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id, PacketWandOption.class, PacketWandOption::encode, PacketWandOption::decode,
                PacketWandOption::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
