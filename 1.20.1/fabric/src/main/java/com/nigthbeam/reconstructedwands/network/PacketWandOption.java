package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.IOption;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketWandOption {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "wand_option");

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

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(key);
        buffer.writeUtf(value);
        buffer.writeBoolean(notify);
    }

    public static PacketWandOption read(FriendlyByteBuf buffer) {
        return new PacketWandOption(buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
    }

    public void handle(ServerPlayer player) {
        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null) {
            return;
        }

        WandOptions options = new WandOptions(wand);
        IOption<?> option = options.get(key);
        if (option == null) {
            return;
        }

        option.setValueString(value);
        options.save();
        if (notify) {
            ItemWand.optionMessage(player, option);
        }
        player.getInventory().setChanged();
    }
}
