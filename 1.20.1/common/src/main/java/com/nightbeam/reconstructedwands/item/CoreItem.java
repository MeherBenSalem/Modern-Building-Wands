package com.nightbeam.reconstructedwands.item;

import com.nightbeam.reconstructedwands.wand.WandMode;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable(mode.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("reconstructedwands.tooltip.core_install").withStyle(ChatFormatting.DARK_GRAY));
    }
}
