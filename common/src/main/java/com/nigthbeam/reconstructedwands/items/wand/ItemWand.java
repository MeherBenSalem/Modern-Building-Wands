package com.nigthbeam.reconstructedwands.items.wand;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.nigthbeam.reconstructedwands.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.IOption;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.wand.WandJob;

import java.util.List;

/**
 * Abstract base class for construction wands.
 */
public abstract class ItemWand extends Item {
    public ItemWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();

        if (world.isClientSide || player == null)
            return InteractionResult.FAIL;

        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching() && REConstructedWands.instance.undoHistory.isUndoActive(player)) {
            return REConstructedWands.instance.undoHistory.undo(player, world, context.getClickedPos())
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        } else {
            WandJob job = getWandJob(player, world,
                    new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(),
                            false),
                    stack);
            return job.doIt() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (world.isClientSide) {
                Services.PLATFORM.openWandGUI(player.getItemInHand(hand));
            }
            return InteractionResult.SUCCESS;
        }

        if (!player.isShiftKeyDown()) { // This condition replaces !player.isCrouching()
            if (world.isClientSide)
                return InteractionResult.PASS; // Changed from InteractionResultHolder.fail(stack) to
                                               // InteractionResult.PASS

            // Right click: Place angel block
            WandJob job = getWandJob(player, world, BlockHitResult.miss(player.getLookAngle(),
                    WandUtil.fromVector(player.getLookAngle()), player.blockPosition()), stack);
            return job.doIt() ? InteractionResult.SUCCESS : InteractionResult.FAIL; // Changed from
                                                                                    // InteractionResultHolder.success/fail(stack)
        }
        return InteractionResult.PASS; // Changed from InteractionResultHolder.fail(stack) to InteractionResult.PASS
    }

    public static WandJob getWandJob(Player player, Level world, BlockHitResult rayTraceResult, ItemStack wand) {
        WandJob wandJob = new WandJob(player, world, rayTraceResult, wand);
        wandJob.getSnapshots();
        return wandJob;
    }

    /**
     * Get remaining durability for placement limits.
     */
    public abstract int remainingDurability(ItemStack stack);

    /**
     * Check if this is an infinity wand (for creative mode limits).
     */
    public abstract boolean isInfinityWand();

    @Override
    public void appendHoverText(ItemStack itemstack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        WandOptions options = new WandOptions(itemstack);
        int limit = options.cores.get().getWandAction().getLimit(itemstack);

        String langTooltip = Constants.MOD_ID + ".tooltip.";

        // +SHIFT tooltip: show all options + installed cores
        if (Screen.hasShiftDown()) {
            for (int i = 1; i < options.allOptions.length; i++) {
                IOption<?> opt = options.allOptions[i];
                lines.add(Component.translatable(opt.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(opt.getValueTranslation()).withStyle(ChatFormatting.GRAY)));
            }
            if (!options.cores.getUpgrades().isEmpty()) {
                lines.add(Component.literal(""));
                lines.add(Component.translatable(langTooltip + "cores").withStyle(ChatFormatting.GRAY));

                for (IWandCore core : options.cores.getUpgrades()) {
                    lines.add(Component
                            .translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
                }
            }
        }
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            lines.add(Component.translatable(langTooltip + "blocks", limit).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(opt.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable(opt.getValueTranslation()).withStyle(ChatFormatting.WHITE)));
            lines.add(Component.translatable(langTooltip + "shift").withStyle(ChatFormatting.AQUA));
        }
    }

    public static void optionMessage(Player player, IOption<?> option) {
        player.displayClientMessage(
                Component.translatable(option.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(option.getValueTranslation()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable(option.getDescTranslation()).withStyle(ChatFormatting.WHITE)),
                true);
    }
}
