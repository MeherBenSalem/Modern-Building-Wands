package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client to server: undo or redo with the wand in hand, or the option key going down or up (sneak + right-click with
 * the option key held undoes, as in 1.x).
 */
public record CommandPayload(int command) implements CustomPacketPayload {
    public static final int UNDO = 0;
    public static final int REDO = 1;
    public static final int OPTION_DOWN = 2;
    public static final int OPTION_UP = 3;

    public static final Type<CommandPayload> TYPE = new Type<>(ReconstructedWands.id("command"));
    public static final StreamCodec<FriendlyByteBuf, CommandPayload> CODEC = CustomPacketPayload.codec(CommandPayload::write, CommandPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(command);
    }

    private static CommandPayload read(FriendlyByteBuf buf) {
        return new CommandPayload(buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
