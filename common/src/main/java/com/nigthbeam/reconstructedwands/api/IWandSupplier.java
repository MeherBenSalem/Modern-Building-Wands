package com.nigthbeam.reconstructedwands.api;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.nigthbeam.reconstructedwands.wand.undo.PlaceSnapshot;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for block suppliers that provide blocks for wand placement.
 */
public interface IWandSupplier {
    /**
     * Get available blocks for placement.
     */
    void getSupply(@Nullable BlockItem target);

    /**
     * Get a snapshot for placing a block at the given position.
     */
    @Nullable
    PlaceSnapshot getPlaceSnapshot(Level world, net.minecraft.core.BlockPos pos,
            BlockHitResult rayTraceResult, BlockState supportingBlock);

    /**
     * Take items from the player's inventory. Returns the number that could NOT be
     * taken.
     */
    int takeItemStack(net.minecraft.world.item.ItemStack stack);
}
