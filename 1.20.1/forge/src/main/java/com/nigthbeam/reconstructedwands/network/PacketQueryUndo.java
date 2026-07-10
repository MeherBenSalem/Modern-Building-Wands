package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.REConstructedWands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketQueryUndo(boolean undoPressed) {
    public static void encode(PacketQueryUndo message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.undoPressed);
    }

    public static PacketQueryUndo decode(FriendlyByteBuf buffer) {
        return new PacketQueryUndo(buffer.readBoolean());
    }

    public static void handle(PacketQueryUndo message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                REConstructedWands.instance.undoHistory.updateClient(player, message.undoPressed);
            }
        });
        context.setPacketHandled(true);
    }
}
