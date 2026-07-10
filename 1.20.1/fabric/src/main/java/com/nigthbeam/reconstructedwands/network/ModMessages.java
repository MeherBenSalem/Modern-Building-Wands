package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class ModMessages {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(PacketWandOption.ID,
                (server, player, handler, buffer, responseSender) -> {
                    PacketWandOption packet = PacketWandOption.read(buffer);
                    server.execute(() -> packet.handle(player));
                });
        ServerPlayNetworking.registerGlobalReceiver(PacketQueryUndo.ID,
                (server, player, handler, buffer, responseSender) -> {
                    PacketQueryUndo packet = PacketQueryUndo.read(buffer);
                    server.execute(() -> packet.handle(player));
                });
        Constants.LOG.info("Registered Fabric networking messages");
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PacketUndoBlocks.ID,
                (client, handler, buffer, responseSender) -> {
                    PacketUndoBlocks packet = PacketUndoBlocks.read(buffer);
                    client.execute(() -> com.nigthbeam.reconstructedwands.client.RenderBlockPreview.undoBlocks = packet.positions);
                });
    }

    public static void sendToServer(Object packet) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        if (packet instanceof PacketWandOption option) {
            option.write(buffer);
            ClientPlayNetworking.send(PacketWandOption.ID, buffer);
        } else if (packet instanceof PacketQueryUndo query) {
            query.write(buffer);
            ClientPlayNetworking.send(PacketQueryUndo.ID, buffer);
        }
    }

    public static void sendToPlayer(PacketUndoBlocks packet, net.minecraft.server.level.ServerPlayer player) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        packet.write(buffer);
        ServerPlayNetworking.send(player, PacketUndoBlocks.ID, buffer);
    }
}
