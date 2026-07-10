package com.nigthbeam.reconstructedwands.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Platform-specific block event handling.
 */
public interface IBlockEventHelper {
    /**
     * Place a block with proper event firing for the platform.
     * Returns true if placement was successful and not cancelled.
     */
    boolean placeBlock(Level world, Player player, BlockState block, BlockPos pos, @Nullable BlockItem item);

    /**
     * Remove a block with proper event firing for the platform.
     * Returns true if removal was successful and not cancelled.
     */
    boolean removeBlock(Level world, Player player, @Nullable BlockState expectedBlock, BlockPos pos);
}
