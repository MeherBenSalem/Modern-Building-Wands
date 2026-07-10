package com.nigthbeam.reconstructedwands.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.nigthbeam.reconstructedwands.platform.services.IBlockEventHelper;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge block event helper for placement and removal with events.
 */
public class NeoForgeBlockEventHelper implements IBlockEventHelper {

    @Override
    public boolean placeBlock(Level world, Player player, BlockState state, BlockPos pos, @Nullable BlockItem item) {
        if (world.isClientSide)
            return false;

        // Set the block first
        boolean placed = world.setBlock(pos, state, 3);

        if (placed) {
            // Fire block place event
            BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                    net.neoforged.neoforge.common.util.BlockSnapshot.create(world.dimension(), world, pos),
                    world.getBlockState(pos.below()),
                    player);
            NeoForge.EVENT_BUS.post(placeEvent);

            if (placeEvent.isCanceled()) {
                // Undo the placement if event was cancelled
                world.removeBlock(pos, false);
                return false;
            }
        }

        return placed;
    }

    @Override
    public boolean removeBlock(Level world, Player player, @Nullable BlockState expectedBlock, BlockPos pos) {
        if (world.isClientSide)
            return false;

        BlockState currentState = world.getBlockState(pos);

        // Fire break event
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(
                world,
                pos,
                currentState,
                player);
        NeoForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        // Remove the block
        return world.destroyBlock(pos, !player.isCreative());
    }
}
