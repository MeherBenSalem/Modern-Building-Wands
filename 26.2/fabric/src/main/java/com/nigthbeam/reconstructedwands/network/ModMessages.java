package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ModMessages {
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(PacketWandOption.TYPE, PacketWandOption.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketQueryUndo.TYPE, PacketQueryUndo.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketUndoBlocks.TYPE, PacketUndoBlocks.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketWandOption.TYPE, PacketWandOption::handle);
        ServerPlayNetworking.registerGlobalReceiver(PacketQueryUndo.TYPE, PacketQueryUndo::handle);
        Constants.LOG.info("Registered Fabric networking messages");
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PacketUndoBlocks.TYPE,
                (packet, context) -> context.client().execute(
                        () -> com.nigthbeam.reconstructedwands.client.RenderBlockPreview.undoBlocks = packet.positions()));
    }

    public static void sendToPlayer(CustomPacketPayload payload, net.minecraft.server.level.ServerPlayer player) {
        ServerPlayNetworking.send(player, payload);
    }
}
