package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PacketQueryUndo(boolean undoPressed) {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "query_undo");
    public void write(FriendlyByteBuf buffer) { buffer.writeBoolean(undoPressed); }
    public static PacketQueryUndo read(FriendlyByteBuf buffer) { return new PacketQueryUndo(buffer.readBoolean()); }
    public void handle(ServerPlayer player) { REConstructedWands.instance.undoHistory.updateClient(player, undoPressed); }
}
