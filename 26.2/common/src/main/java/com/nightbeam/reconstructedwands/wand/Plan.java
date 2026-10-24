package com.nightbeam.reconstructedwands.wand;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** What one click of a wand would do: computed on the client for the preview and on the server to act. */
public final class Plan {
    public enum Kind { PLACE, REMOVE, SWAP }

    /**
     * One position. {@code item} and {@code state} are the block to put there (null for a removal); {@code against}
     * is the block it is placed against (for events and orientation).
     */
    public record Target(BlockPos pos, Kind kind, @Nullable BlockItem item, @Nullable BlockState state, BlockState against) {
    }

    public final WandMode mode;
    public final int limit;
    public final List<Target> targets = new ArrayList<>();
    /** Positions that would be built if the player had the blocks. */
    public final List<BlockPos> missing = new ArrayList<>();
    /** Why nothing can happen, for the HUD (null when the plan is fine or simply empty). */
    public @Nullable Component problem;
    /** Architect: the stored first corner (null until it is set). */
    public @Nullable BlockPos anchor;
    /** Architect: the corner the click is aimed at (the first corner if none is stored yet). */
    public @Nullable BlockPos corner;

    public Plan(WandMode mode, int limit) {
        this.mode = mode;
        this.limit = limit;
    }

    public static Plan empty(WandMode mode, @Nullable Component problem) {
        Plan plan = new Plan(mode, 0);
        plan.problem = problem;
        return plan;
    }

    public boolean isEmpty() {
        return targets.isEmpty();
    }

    /** The item most of the plan uses, for the HUD. */
    public @Nullable BlockItem mainItem() {
        for (Target target : targets) {
            if (target.item() != null) {
                return target.item();
            }
        }
        return null;
    }
}
