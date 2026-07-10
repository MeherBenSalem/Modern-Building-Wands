package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.basics.ReplacementRegistry;
import com.nigthbeam.reconstructedwands.platform.services.IBlockEventHelper;

import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of IBlockEventHelper.
 */
public class FabricBlockEventHelper implements IBlockEventHelper {
    @Override
    public boolean placeBlock(Level world, Player player, BlockState block, BlockPos pos, @Nullable BlockItem item) {
        if (!world.setBlockAndUpdate(pos, block)) {
            Constants.LOG.info("Block could not be placed");
            return false;
        }

        // Fabric uses PlayerBlockPlacementCallback, but for now we proceed directly
        // In a full implementation, you would post an event here

        ItemStack stack;
        if (item == null) {
            stack = new ItemStack(block.getBlock().asItem());
        } else {
            stack = new ItemStack(item);
            player.awardStat(Stats.ITEM_USED.get(item));
        }

        // Call OnBlockPlaced method
        block.getBlock().setPlacedBy(world, pos, block, player, stack);

        return true;
    }

    @Override
    public boolean removeBlock(Level world, Player player, @Nullable BlockState expectedBlock, BlockPos pos) {
        BlockState currentBlock = world.getBlockState(pos);

        if (!world.mayInteract(player, pos))
            return false;

        if (!player.isCreative()) {
            if (currentBlock.getDestroySpeed(world, pos) <= -1 || world.getBlockEntity(pos) != null) {
                return false;
            }

            if (expectedBlock != null) {
                if (!ReplacementRegistry.matchBlocks(currentBlock.getBlock(), expectedBlock.getBlock())) {
                    return false;
                }
            }
        }

        // Fabric uses PlayerBlockBreakEvents, but for now we proceed directly
        // In a full implementation, you would post an event here

        world.removeBlock(pos, false);
        return true;
    }
}
