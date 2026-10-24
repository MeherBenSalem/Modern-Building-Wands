package com.nightbeam.reconstructedwands.platform;

import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/** What common code needs from the loader. Each loader passes an implementation to {@code ReconstructedWands.init}. */
public interface Platform {
    Path configDir();

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** True for automation players (deployers, turtles, fake players of other mods). */
    boolean isFakePlayer(ServerPlayer player);

    boolean isClient();

    /**
     * Asks claim and protection mods whether {@code player} may place a block at {@code pos}, before any item moves.
     * Loaders whose protection hooks only exist as post-placement events answer {@code true} and veto in
     * {@link #placeBlock} instead.
     */
    boolean mayPlace(ServerLevel level, BlockPos pos, ServerPlayer player);

    /** Asks claim and protection mods (and the loader's cancellable break event) whether the block may be removed. */
    boolean mayBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player);

    /**
     * Sets {@code state} at {@code pos} as a placement by {@code player} against {@code against}, firing the loader's
     * placement event. A vetoed placement is rolled back to the exact previous block and returns false.
     */
    boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockState against, ServerPlayer player);

    /** Tells listeners that {@code player} removed {@code state} at {@code pos} (after the fact). */
    void afterBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player);
}
