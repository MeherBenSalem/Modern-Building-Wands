package com.nigthbeam.reconstructedwands.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.REConstructedWandsNeoForge;

import java.util.HashSet;
import java.util.Set;

public class PacketUndoBlocks implements CustomPacketPayload {
    public static final Type<PacketUndoBlocks> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "undo_blocks"));

    public static final StreamCodec<FriendlyByteBuf, PacketUndoBlocks> STREAM_CODEC = StreamCodec.of(
            (b, v) -> PacketUndoBlocks.encode(v, b),
            PacketUndoBlocks::decode);

    public HashSet<BlockPos> undoBlocks;

    public PacketUndoBlocks(Set<BlockPos> undoBlocks) {
        this.undoBlocks = new HashSet<>(undoBlocks);
    }

    private PacketUndoBlocks(HashSet<BlockPos> undoBlocks) {
        this.undoBlocks = undoBlocks;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(PacketUndoBlocks msg, FriendlyByteBuf buffer) {
        for (BlockPos pos : msg.undoBlocks) {
            buffer.writeBlockPos(pos);
        }
    }

    public static PacketUndoBlocks decode(FriendlyByteBuf buffer) {
        HashSet<BlockPos> undoBlocks = new HashSet<>();
        while (buffer.readableBytes() > 0) {
            try {
                undoBlocks.add(buffer.readBlockPos());
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return new PacketUndoBlocks(undoBlocks);
    }

    public static void handle(final PacketUndoBlocks msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            REConstructedWandsNeoForge.instance.renderBlockPreview.undoBlocks = msg.undoBlocks;
        });
    }
}
