package com.nigthbeam.reconstructedwands.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.IOption;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;

public class PacketWandOption implements CustomPacketPayload {
    public static final Type<PacketWandOption> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wand_option"));

    public static final StreamCodec<FriendlyByteBuf, PacketWandOption> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of((b, v) -> b.writeUtf(v), FriendlyByteBuf::readUtf),
            p -> p.key,
            StreamCodec.of((b, v) -> b.writeUtf(v), FriendlyByteBuf::readUtf),
            p -> p.value,
            StreamCodec.of((b, v) -> b.writeBoolean(v), FriendlyByteBuf::readBoolean),
            p -> p.notify,
            PacketWandOption::new);

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketWandOption msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null)
                return;

            ItemStack wand = WandUtil.holdingWand(player);
            if (wand == null)
                return;
            WandOptions options = new WandOptions(wand);

            IOption<?> option = options.get(msg.key);
            if (option == null)
                return;
            option.setValueString(msg.value);
            options.save(); // Persist changes to ItemStack NBT

            if (msg.notify)
                ItemWand.optionMessage(player, option);
            player.getInventory().setChanged();
        });
    }
}
