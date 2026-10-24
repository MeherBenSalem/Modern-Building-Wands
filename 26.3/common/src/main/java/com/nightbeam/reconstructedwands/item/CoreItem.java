package com.nightbeam.reconstructedwands.item;

import com.nightbeam.reconstructedwands.wand.WandMode;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A core: craft it together with a wand to give the wand another mode. */
public final class CoreItem extends Item {
    private final WandMode mode;

    public CoreItem(WandMode mode, Properties properties) {
        super(properties);
        this.mode = mode;
    }

    public WandMode mode() {
        return mode;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable(mode.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("reconstructedwands.tooltip.core_install").withStyle(ChatFormatting.DARK_GRAY));
    }
}
