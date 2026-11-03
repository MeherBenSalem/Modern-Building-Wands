package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.content.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The checks every position passes before a wand touches it. They run on both sides (the client uses them for the
 * preview); the server additionally asks the loader's protection hooks when it executes.
 */
public final class Rules {
    private Rules() {
    }

    /** In the world, in range, and the player may change blocks here at all. */
    public static boolean modifiable(Level level, Player player, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.mayInteract(player, pos) || !player.mayBuild()) {
            return false;
        }
        int range = WandsConfig.server().maxRange;
        return range <= 0 || pos.distToCenterSqr(player.position()) <= (double) range * range;
    }

    /** Whether a block could go at {@code pos}: air, or a replaceable block (grass, water...) when replace is on. */
    public static boolean placeable(Level level, Player player, BlockPos pos, boolean replace) {
        if (!modifiable(level, player, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        return replace && state.canBeReplaced() && !denied(state, player) && !state.hasBlockEntity();
    }

    /** Whether a wand may remove {@code state} at {@code pos} (Destruction, and the old block of an Exchange). */
    public static boolean removable(Level level, Player player, BlockPos pos, BlockState state, boolean exchange) {
        if (state.isAir() || !modifiable(level, player, pos) || denied(state, player) || state.is(ModTags.NO_DISMANTLE)) {
            return false;
        }
        if (state.hasBlockEntity() && (exchange || !player.isCreative())) {
            return false;
        }
        return player.isCreative() || state.getDestroySpeed(level, pos) >= 0;
    }

    /**
     * Whether wands must leave {@code state} alone. Blocks in {@code #reconstructedwands:creative_only} (barriers, light
     * blocks, structure voids) are allowed for creative-mode players, who can place them by hand anyway.
     */
    public static boolean denied(BlockState state, Player player) {
        return state.is(ModTags.DENIED) && !(player.isCreative() && state.is(ModTags.CREATIVE_ONLY));
    }

    /** True if a living entity (not a spectator) is in the way of {@code state} at {@code pos}. */
    public static boolean blockedByEntity(Level level, BlockState state, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AABB box = shape.bounds().move(pos);
        return !level.getEntitiesOfClass(LivingEntity.class, box, entity -> !entity.isSpectator() && entity.isAlive()).isEmpty();
    }

    /** True if nothing solid covers the face of {@code pos} toward {@code side} (Destruction and Exchange only touch exposed faces). */
    public static boolean exposed(Level level, BlockPos pos, Direction side) {
        BlockPos front = pos.relative(side);
        BlockState state = level.getBlockState(front);
        return state.isAir() || state.getCollisionShape(level, front).isEmpty();
    }
}
