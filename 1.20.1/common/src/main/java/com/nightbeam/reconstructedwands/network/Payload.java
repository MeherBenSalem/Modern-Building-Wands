package com.nightbeam.reconstructedwands.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * A message between client and server. 1.20.1 has no {@code CustomPacketPayload}, so each loader sends these on a
 * channel named by {@link #id()} (the same ids as the 1.21 payload types). Each payload also has a static
 * {@code read(FriendlyByteBuf)}.
 */
public interface Payload {
    ResourceLocation id();

    void write(FriendlyByteBuf buf);
}
