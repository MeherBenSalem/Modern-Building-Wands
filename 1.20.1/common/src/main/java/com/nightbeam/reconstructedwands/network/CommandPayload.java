package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: undo or redo with the wand in hand, or the option key going down or up (sneak + right-click with
 * the option key held undoes, as in 1.x).
 */
public record CommandPayload(int command) implements Payload {
    public static final int UNDO = 0;
    public static final int REDO = 1;
    public static final int OPTION_DOWN = 2;
    public static final int OPTION_UP = 3;

    public static final ResourceLocation ID = ReconstructedWands.id("command");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(command);
    }

    public static CommandPayload read(FriendlyByteBuf buf) {
        return new CommandPayload(buf.readVarInt());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
