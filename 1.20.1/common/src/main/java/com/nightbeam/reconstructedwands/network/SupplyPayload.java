package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: blocks the client cannot see itself (the Ender Chest and the bound container), by item id, so the
 * preview and the HUD count them.
 */
public record SupplyPayload(Map<String, Integer> counts) implements Payload {
    public static final int MAX_ENTRIES = 256;

    public static final ResourceLocation ID = ReconstructedWands.id("supply");

    @Override
    public void write(FriendlyByteBuf buf) {
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

    public static SupplyPayload read(FriendlyByteBuf buf) {
        int count = Math.min(buf.readVarInt(), MAX_ENTRIES);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            counts.put(buf.readUtf(), buf.readVarInt());
        }
        return new SupplyPayload(counts);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
