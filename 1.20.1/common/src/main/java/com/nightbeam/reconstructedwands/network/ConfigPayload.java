package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server to client: the server's server.json, so previews and the HUD follow the server's rules. */
public record ConfigPayload(String json) implements Payload {
    public static final ResourceLocation ID = ReconstructedWands.id("config");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(json, 262_144);
    }

    public static ConfigPayload read(FriendlyByteBuf buf) {
        return new ConfigPayload(buf.readUtf(262_144));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
