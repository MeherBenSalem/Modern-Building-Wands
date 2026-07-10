package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.HashSet;
import java.util.Set;

public record PacketUndoBlocks(Set<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<PacketUndoBlocks> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "undo_blocks"));
    public static final StreamCodec<FriendlyByteBuf, PacketUndoBlocks> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeVarInt(packet.positions.size());
                packet.positions.forEach(buffer::writeBlockPos);
            }, buffer -> {
                Set<BlockPos> positions = new HashSet<>();
                int size = buffer.readVarInt();
                for (int i = 0; i < size; i++) positions.add(buffer.readBlockPos());
                return new PacketUndoBlocks(positions);
            });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
