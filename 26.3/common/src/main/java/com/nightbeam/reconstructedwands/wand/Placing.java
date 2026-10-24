package com.nightbeam.reconstructedwands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Works out the exact block state a wand puts at a position, the way a player's own placement would. */
public final class Placing {
    /** Orientation properties copied from the block a placement extends (the "Target" direction setting). */
    @SuppressWarnings("rawtypes")
    private static final Property[] ORIENTATION = {
            BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.FACING, BlockStateProperties.FACING_HOPPER,
            BlockStateProperties.ROTATION_16, BlockStateProperties.AXIS, BlockStateProperties.HORIZONTAL_AXIS,
            BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE, BlockStateProperties.ATTACH_FACE
    };

    private Placing() {
    }

    /** A placement context aimed at {@code pos}, as if the player clicked the same spot of the face they clicked. */
    private static final class Context extends BlockPlaceContext {
        Context(Level level, Player player, BlockHitResult hit, BlockPos pos, BlockItem item) {
            super(level, player, InteractionHand.MAIN_HAND, new ItemStack(item),
                    new BlockHitResult(shifted(hit, pos), hit.getDirection(), pos, false));
        }

        @Override
        public boolean canPlace() {
            return replaceClicked;
        }

        private static Vec3 shifted(BlockHitResult hit, BlockPos pos) {
            BlockPos from = hit.getBlockPos();
            return hit.getLocation().add(pos.getX() - from.getX(), pos.getY() - from.getY(), pos.getZ() - from.getZ());
        }
    }

    /** A hit on the top face of {@code pos}, for placements that have no clicked face (mid-air, shapes). */
    public static BlockHitResult syntheticHit(BlockPos pos, Direction face) {
        Vec3 center = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        return new BlockHitResult(center, face, pos, false);
    }

    /**
     * The state {@code item} would place at {@code pos}, or null if it cannot go there (occupied, cannot survive,
     * an entity is in the way, or the block is denied). {@code copyFrom} supplies the orientation in Target mode.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static @Nullable BlockState stateFor(Level level, Player player, BlockHitResult hit, BlockPos pos, BlockItem item,
                                                @Nullable BlockState copyFrom) {
        BlockPlaceContext context = new Context(level, player, hit, pos, item);
        if (!context.canPlace()) {
            return null;
        }
        BlockState state = item.getBlock().getStateForPlacement(context);
        // Two-block blocks (beds, doors, tall plants) place their other half themselves; wands leave them alone.
        if (state == null || Rules.denied(state) || state.hasProperty(BlockStateProperties.BED_PART)
                || state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return null;
        }
        if (copyFrom != null) {
            state = orient(state, copyFrom);
        }
        if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE
                && !level.getBlockState(pos).isAir()) {
            return null;
        }
        if (!state.canSurvive(level, pos) || Rules.blockedByEntity(level, state, pos)) {
            return null;
        }
        return state;
    }

    /**
     * {@code state} turned the way {@code from} is turned (facing, axis, half, stair shape, slab half). A double slab
     * is never copied: it would place two slabs for one item.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BlockState orient(BlockState state, BlockState from) {
        for (Property property : ORIENTATION) {
            if (from.hasProperty(property) && state.hasProperty(property)) {
                state = state.setValue(property, from.getValue(property));
            }
        }
        if (from.hasProperty(BlockStateProperties.SLAB_TYPE) && state.hasProperty(BlockStateProperties.SLAB_TYPE)
                && from.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
            state = state.setValue(BlockStateProperties.SLAB_TYPE, from.getValue(BlockStateProperties.SLAB_TYPE));
        }
        return state;
    }
}
