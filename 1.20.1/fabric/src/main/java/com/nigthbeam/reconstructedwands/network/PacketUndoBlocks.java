package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.HashSet;
import java.util.Set;

public class PacketUndoBlocks {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "undo_blocks");
    public final Set<BlockPos> positions;
    public PacketUndoBlocks(Set<BlockPos> positions) { this.positions = new HashSet<>(positions); }
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(positions.size());
        positions.forEach(buffer::writeBlockPos);
    }
    public static PacketUndoBlocks read(FriendlyByteBuf buffer) {
        Set<BlockPos> positions = new HashSet<>();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) positions.add(buffer.readBlockPos());
        return new PacketUndoBlocks(positions);
    }
}
