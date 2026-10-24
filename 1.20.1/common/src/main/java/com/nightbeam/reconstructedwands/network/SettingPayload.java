package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client to server: change one setting of the wand in hand ({@code setting} is a {@code WandSettings.Setting} ordinal). */
public record SettingPayload(int setting, int value) implements Payload {
    public static final ResourceLocation ID = ReconstructedWands.id("setting");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(setting);
        buf.writeVarInt(value);
    }

    public static SettingPayload read(FriendlyByteBuf buf) {
        return new SettingPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
