package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: blocks the client cannot see itself (the Ender Chest and the bound container), by item id, so the
 * preview and the HUD count them.
 */
public record SupplyPayload(Map<String, Integer> counts) implements CustomPacketPayload {
    public static final int MAX_ENTRIES = 256;

    public static final Type<SupplyPayload> TYPE = new Type<>(ReconstructedWands.id("supply"));
    public static final StreamCodec<FriendlyByteBuf, SupplyPayload> CODEC = CustomPacketPayload.codec(SupplyPayload::write, SupplyPayload::read);

    private void write(FriendlyByteBuf buf) {
        int count = Math.min(counts.size(), MAX_ENTRIES);
        buf.writeVarInt(count);
        int i = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (i++ == count) {
                break;
            }
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    private static SupplyPayload read(FriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_ENTRIES);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            counts.put(buf.readUtf(), buf.readVarInt());
        }
        return new SupplyPayload(counts);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
