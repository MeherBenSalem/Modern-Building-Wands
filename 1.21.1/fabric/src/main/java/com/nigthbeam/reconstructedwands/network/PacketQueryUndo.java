package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacketQueryUndo(boolean undoPressed) implements CustomPacketPayload {
    public static final Type<PacketQueryUndo> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "query_undo"));
    public static final StreamCodec<FriendlyByteBuf, PacketQueryUndo> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBoolean(packet.undoPressed),
            buffer -> new PacketQueryUndo(buffer.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(PacketQueryUndo packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> REConstructedWands.instance.undoHistory.updateClient(context.player(), packet.undoPressed));
    }
}
