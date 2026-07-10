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
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.platform.Services;
import com.nigthbeam.reconstructedwands.wand.undo.DestroySnapshot;
import com.nigthbeam.reconstructedwands.wand.undo.ISnapshot;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Destruction action: break multiple blocks at once.
 */
public class ActionDestruction implements IWandAction {
    @Override
    public int getLimit(ItemStack wand) {
        String wandType = getWandType(wand);
        return Services.CONFIG.getWandDestruction(wandType);
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> destroySnapshots = new LinkedList<>();
        LinkedList<BlockPos> candidates = new LinkedList<>();
        HashSet<BlockPos> allCandidates = new HashSet<>();

        Direction breakFace = rayTraceResult.getDirection();
        BlockPos startingPoint = rayTraceResult.getBlockPos();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());

        // Is break direction allowed by lock?
        if (breakFace == Direction.UP || breakFace == Direction.DOWN) {
            if (options.testLock(WandOptions.LOCK.NORTHSOUTH) || options.testLock(WandOptions.LOCK.EASTWEST))
                candidates.add(startingPoint);
        } else if (options.testLock(WandOptions.LOCK.HORIZONTAL) || options.testLock(WandOptions.LOCK.VERTICAL))
            candidates.add(startingPoint);

        while (!candidates.isEmpty() && destroySnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();

            // Only break blocks facing the player
            if (!WandUtil.isBlockPermeable(world, currentCandidate.relative(breakFace)))
                continue;

            try {
                BlockState candidateBlock = world.getBlockState(currentCandidate);

                if (options.matchBlocks(targetBlock.getBlock(), candidateBlock.getBlock()) &&
                        allCandidates.add(currentCandidate)) {
                    DestroySnapshot snapshot = DestroySnapshot.get(world, player, currentCandidate);
                    if (snapshot == null)
                        continue;
                    destroySnapshots.add(snapshot);

                    switch (breakFace) {
                        case DOWN, UP -> {
                            if (options.testLock(WandOptions.LOCK.NORTHSOUTH)) {
                                candidates.add(currentCandidate.relative(Direction.NORTH));
                                candidates.add(currentCandidate.relative(Direction.SOUTH));
                            }
                            if (options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.WEST));
                            }
                            if (options.testLock(WandOptions.LOCK.NORTHSOUTH)
                                    && options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.relative(Direction.NORTH)
                                        .relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.NORTH)
                                        .relative(Direction.WEST));
                                candidates.add(currentCandidate.relative(Direction.SOUTH)
                                        .relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.SOUTH)
                                        .relative(Direction.WEST));
                            }
                        }
                        case NORTH, SOUTH -> {
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.WEST));
                            }
                            if (options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.relative(Direction.UP));
                                candidates.add(currentCandidate.relative(Direction.DOWN));
                            }
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)
                                    && options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.relative(Direction.UP)
                                        .relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.UP)
                                        .relative(Direction.WEST));
                                candidates.add(currentCandidate.relative(Direction.DOWN)
                                        .relative(Direction.EAST));
                                candidates.add(currentCandidate.relative(Direction.DOWN)
                                        .relative(Direction.WEST));
                            }
                        }
                        case EAST, WEST -> {
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.relative(Direction.NORTH));
                                candidates.add(currentCandidate.relative(Direction.SOUTH));
                            }
                            if (options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.relative(Direction.UP));
                                candidates.add(currentCandidate.relative(Direction.DOWN));
                            }
                            if (options.testLock(WandOptions.LOCK.HORIZONTAL)
                                    && options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.relative(Direction.UP)
                                        .relative(Direction.NORTH));
                                candidates.add(currentCandidate.relative(Direction.UP)
                                        .relative(Direction.SOUTH));
                                candidates.add(currentCandidate.relative(Direction.DOWN)
                                        .relative(Direction.NORTH));
                                candidates.add(currentCandidate.relative(Direction.DOWN)
                                        .relative(Direction.SOUTH));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Skip if anything goes wrong
            }
        }
        return destroySnapshots;
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
