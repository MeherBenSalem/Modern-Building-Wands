package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.IOption;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketWandOption {
    public final String key;
    public final String value;
    public final boolean notify;

    public PacketWandOption(IOption<?> option, boolean notify) {
        this(option.getKey(), option.getValueString(), notify);
    }

    private PacketWandOption(String key, String value, boolean notify) {
        this.key = key;
        this.value = value;
        this.notify = notify;
    }

    public static void encode(PacketWandOption message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.key);
        buffer.writeUtf(message.value);
        buffer.writeBoolean(message.notify);
    }

    public static PacketWandOption decode(FriendlyByteBuf buffer) {
        return new PacketWandOption(buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(PacketWandOption message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ItemStack wand = WandUtil.holdingWand(player);
            if (wand == null) return;
            WandOptions options = new WandOptions(wand);
            IOption<?> option = options.get(message.key);
            if (option == null) return;
            option.setValueString(message.value);
            options.save();
            if (message.notify) ItemWand.optionMessage(player, option);
            player.getInventory().setChanged();
        });
        context.setPacketHandled(true);
    }
}
