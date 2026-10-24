package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server to client: the positions of the action Ctrl+Z would undo, and whether anything can be redone. */
public record HistoryPayload(List<BlockPos> undo, boolean canRedo) implements Payload {
    public static final int MAX_POSITIONS = 8192;

    public static final ResourceLocation ID = ReconstructedWands.id("history");

    @Override
    public void write(FriendlyByteBuf buf) {
        int count = Math.min(undo.size(), MAX_POSITIONS);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeLong(undo.get(i).asLong());
        }
        buf.writeBoolean(canRedo);
    }

    public static HistoryPayload read(FriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_POSITIONS);
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(BlockPos.of(buf.readLong()));
        }
        return new HistoryPayload(positions, buf.readBoolean());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
