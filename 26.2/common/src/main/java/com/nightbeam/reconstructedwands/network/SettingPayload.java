package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: change one setting of the wand in hand ({@code setting} is a {@code WandSettings.Setting} ordinal). */
public record SettingPayload(int setting, int value) implements CustomPacketPayload {
    public static final Type<SettingPayload> TYPE = new Type<>(ReconstructedWands.id("setting"));
    public static final StreamCodec<FriendlyByteBuf, SettingPayload> CODEC = CustomPacketPayload.codec(SettingPayload::write, SettingPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(setting);
        buf.writeVarInt(value);
    }

    private static SettingPayload read(FriendlyByteBuf buf) {
        return new SettingPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
