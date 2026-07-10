package com.nigthbeam.reconstructedwands.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import com.nigthbeam.reconstructedwands.Constants;

public class PacketQueryUndo implements CustomPacketPayload {
    public static final Type<PacketQueryUndo> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "query_undo"));

    public static final StreamCodec<FriendlyByteBuf, PacketQueryUndo> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of((b, v) -> b.writeBoolean(v), FriendlyByteBuf::readBoolean),
            p -> p.undoPressed,
            PacketQueryUndo::new);

    public boolean undoPressed;

    public PacketQueryUndo(boolean undoPressed) {
        this.undoPressed = undoPressed;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketQueryUndo msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null)
                return;

            REConstructedWands.instance.undoHistory.updateClient(player, msg.undoPressed);
        });
    }
}
