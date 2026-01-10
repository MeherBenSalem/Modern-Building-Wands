package com.nigthbeam.reconstructedwands.wand.action;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandSupplier;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.platform.Services;
import com.nigthbeam.reconstructedwands.wand.undo.ISnapshot;
import com.nigthbeam.reconstructedwands.wand.undo.PlaceSnapshot;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Default WandAction. Extends your building on the side you're facing.
 */
public class ActionConstruction implements IWandAction {
    @Override
    public int getLimit(ItemStack wand) {
        String wandType = getWandType(wand);
        return Services.CONFIG.getWandLimit(wandType);
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();
        LinkedList<BlockPos> candidates = new LinkedList<>();
        HashSet<BlockPos> allCandidates = new HashSet<>();

        Direction placeDirection = rayTraceResult.getDirection();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());
        BlockPos startingPoint = rayTraceResult.getBlockPos().offset(placeDirection.getNormal());

        // Is place direction allowed by lock?
        if (placeDirection == Direction.UP || placeDirection == Direction.DOWN) {
            if (options.testLock(WandOptions.LOCK.NORTHSOUTH) || options.testLock(WandOptions.LOCK.EASTWEST))
                candidates.add(startingPoint);
        } else if (options.testLock(WandOptions.LOCK.HORIZONTAL) || options.testLock(WandOptions.LOCK.VERTICAL))
            candidates.add(startingPoint);

        while (!candidates.isEmpty() && placeSnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();
            try {
                BlockPos supportingPoint = currentCandidate.offset(placeDirection.getOpposite().getNormal());
                BlockState candidateSupportingBlock = world.getBlockState(supportingPoint);

                if (options.matchBlocks(targetBlock.getBlock(), candidateSupportingBlock.getBlock()) &&
                        allCandidates.add(currentCandidate)) {
                    PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentCandidate, rayTraceResult,
                            candidateSupportingBlock);
                    if (snapshot == null)
                        continue;
                    placeSnapshots.add(snapshot);

                    switch (placeDirection) {
                        case DOWN, UP -> {
                            if (options.testLock(WandOptions.LOCK.NORTHSOUTH)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.NORTHSOUTH)
                                    && options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getNormal())
                                        .offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.NORTH.getNormal())
                                        .offset(Direction.WEST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getNormal())
                                        .offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getNormal())
                                        .offset(Direction.WEST.getNormal()));
                            }
                        }
                        case NORTH, SOUTH -> {
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)
                                    && options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal())
                                        .offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal())
                                        .offset(Direction.WEST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal())
                                        .offset(Direction.EAST.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal())
                                        .offset(Direction.WEST.getNormal()));
                            }
                        }
                        case EAST, WEST -> {
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal()));
                            }
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)
                                    && options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal())
                                        .offset(Direction.NORTH.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.UP.getNormal())
                                        .offset(Direction.SOUTH.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal())
                                        .offset(Direction.NORTH.getNormal()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getNormal())
                                        .offset(Direction.SOUTH.getNormal()));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Skip if anything goes wrong
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        return new ArrayList<>();
    }

    protected String getWandType(ItemStack wand) {
        return Services.REGISTRY.getItemKey(wand.getItem()).getPath();
    }
}
