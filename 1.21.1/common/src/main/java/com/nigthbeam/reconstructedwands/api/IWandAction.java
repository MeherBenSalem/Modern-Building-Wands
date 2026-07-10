package com.nigthbeam.reconstructedwands.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.wand.undo.ISnapshot;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Interface for wand placement actions (construction, angel, destruction).
 */
public interface IWandAction {
    /**
     * Get the block placement limit for this action.
     */
    int getLimit(ItemStack wand);

    /**
     * Get block placement snapshots when clicking on a block.
     */
    @NotNull
    List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);

    /**
     * Get block placement snapshots when clicking in mid-air.
     */
    @NotNull
    List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);
}
