package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.REConstructedWandsForge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketUndoBlocks {
    public final HashSet<BlockPos> undoBlocks;

    public PacketUndoBlocks(Set<BlockPos> undoBlocks) {
        this.undoBlocks = new HashSet<>(undoBlocks);
    }

    public static void encode(PacketUndoBlocks message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.undoBlocks.size());
        message.undoBlocks.forEach(buffer::writeBlockPos);
    }

    public static PacketUndoBlocks decode(FriendlyByteBuf buffer) {
        HashSet<BlockPos> positions = new HashSet<>();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) positions.add(buffer.readBlockPos());
        return new PacketUndoBlocks(positions);
    }

    public static void handle(PacketUndoBlocks message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (REConstructedWandsForge.instance.renderBlockPreview != null) {
                REConstructedWandsForge.instance.renderBlockPreview.undoBlocks = message.undoBlocks;
            }
        });
        context.setPacketHandled(true);
    }
}
