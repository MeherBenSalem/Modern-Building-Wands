package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: the server's server.json, so previews and the HUD follow the server's rules. */
public record ConfigPayload(String json) implements CustomPacketPayload {
    public static final Type<ConfigPayload> TYPE = new Type<>(ReconstructedWands.id("config"));
    public static final StreamCodec<FriendlyByteBuf, ConfigPayload> CODEC = CustomPacketPayload.codec(ConfigPayload::write, ConfigPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(json, 262_144);
    }

    private static ConfigPayload read(FriendlyByteBuf buf) {
        return new ConfigPayload(buf.readUtf(262_144));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
