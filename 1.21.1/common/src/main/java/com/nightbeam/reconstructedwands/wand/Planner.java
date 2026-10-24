package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.config.ServerConfig;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.item.WandTier;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Works out a {@link Plan} for each mode. Pure: nothing in the world or inventory changes here. */
public final class Planner {
    private Planner() {
    }

    /**
     * @param hit the clicked block face, or null for a click in the air
     */
    public static Plan plan(Player player, Level level, ItemStack wand, @Nullable BlockHitResult hit, RandomSource rng) {
        if (!(wand.getItem() instanceof WandItem wandItem)) {
            return Plan.empty(WandMode.CONSTRUCTION, null);
        }
        WandSettings settings = WandSettings.of(wand);
        WandMode mode = settings.mode();
        int limit = limit(player, wandItem.tier(), mode, settings.size(), wand);
        if (limit <= 0) {
            return Plan.empty(mode, Component.translatable("reconstructedwands.problem.broken"));
        }
        Supply supply = Supply.of(player, settings);
        return switch (mode) {
            case CONSTRUCTION -> hit == null ? Plan.empty(mode, null) : construction(player, level, hit, settings, supply, limit, rng);
            case ANGEL -> angel(player, level, hit, settings, supply, wandItem.tier(), rng);
            case DESTRUCTION -> hit == null ? Plan.empty(mode, null) : surface(player, level, hit, settings, supply, limit, false, rng);
            case EXCHANGE -> hit == null ? Plan.empty(mode, null) : surface(player, level, hit, settings, supply, limit, true, rng);
            case ARCHITECT -> architect(player, level, hit, settings, supply, limit, rng);
        };
    }

    /** The most blocks one click of this mode may change, after the size setting and the wand's durability. */
    public static int limit(Player player, WandTier tier, WandMode mode, int size, ItemStack wand) {
        ServerConfig.Tier stats = tier.stats();
        int base = switch (mode) {
            case CONSTRUCTION, ARCHITECT -> player.isCreative() && tier == WandTier.INFINITY
                    ? WandsConfig.server().creativeLimit : stats.construction();
            case ANGEL -> 1;
            case DESTRUCTION -> stats.destruction();
            case EXCHANGE -> stats.exchange();
        };
        int limit = size > 0 ? Math.min(size, base) : base;
        if (!player.isCreative() && wand.isDamageableItem()) {
            limit = Math.min(limit, wand.getMaxDamage() - wand.getDamageValue());
        }
        return limit;
    }

    /** The largest size the size setting can take for this wand and mode (ignoring durability). */
    public static int maxSize(Player player, WandTier tier, WandMode mode) {
        ServerConfig.Tier stats = tier.stats();
        return switch (mode) {
            case CONSTRUCTION, ARCHITECT -> player.isCreative() && tier == WandTier.INFINITY
                    ? WandsConfig.server().creativeLimit : stats.construction();
            case ANGEL -> 1;
            case DESTRUCTION -> stats.destruction();
            case EXCHANGE -> stats.exchange();
        };
    }

    // Construction: extend the clicked face across the matching blocks behind it.

    private static Plan construction(Player player, Level level, BlockHitResult hit, WandSettings settings, Supply supply, int limit,
                                     RandomSource rng) {
        Plan plan = new Plan(WandMode.CONSTRUCTION, limit);
        Direction face = hit.getDirection();
        BlockState target = level.getBlockState(hit.getBlockPos());
        Palette palette = Palette.of(player, settings, supply, target.getBlock(), false);
        if (palette.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_block");
            return plan;
        }
        boolean copy = settings.direction() == WandSettings.Facing.TARGET;
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        BlockPos start = hit.getBlockPos().relative(face);
        if (lockAllows(settings, face)) {
            queue.add(start);
        }
        while (!queue.isEmpty() && plan.targets.size() + plan.missing.size() < limit) {
            BlockPos pos = queue.removeFirst();
            if (!seen.add(pos)) {
                continue;
            }
            BlockState supporting = level.getBlockState(pos.relative(face.getOpposite()));
            if (!Matching.matches(settings.match(), target.getBlock(), supporting.getBlock())
                    || !Rules.placeable(level, player, pos, settings.replace())) {
                continue;
            }
            BlockState copyFrom = copy ? supporting : null;
            BlockItem item = palette.take(rng, it -> Placing.stateFor(level, player, hit, pos, it, copyFrom) != null);
            if (item != null) {
                plan.targets.add(new Plan.Target(pos, Plan.Kind.PLACE, item, Placing.stateFor(level, player, hit, pos, item, copyFrom), supporting));
            } else if (palette.first() != null && Placing.stateFor(level, player, hit, pos, palette.first(), copyFrom) != null) {
                plan.missing.add(pos);
            } else {
                continue;
            }
            spread(queue, pos, face, settings);
        }
        if (plan.targets.isEmpty() && !plan.missing.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_items");
        }
        return plan;
    }

    // Destruction and Exchange: the exposed surface of matching blocks.

    private static Plan surface(Player player, Level level, BlockHitResult hit, WandSettings settings, Supply supply, int limit,
                                boolean exchange, RandomSource rng) {
        WandMode mode = exchange ? WandMode.EXCHANGE : WandMode.DESTRUCTION;
        Plan plan = new Plan(mode, limit);
        Direction face = hit.getDirection();
        BlockState target = level.getBlockState(hit.getBlockPos());
        Palette palette = null;
        if (exchange) {
            palette = Palette.of(player, settings, supply, null, false);
            if (palette.isEmpty()) {
                plan.problem = Component.translatable("reconstructedwands.problem.no_replacement");
                return plan;
            }
        }
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        if (lockAllows(settings, face)) {
            queue.add(hit.getBlockPos());
        }
        while (!queue.isEmpty() && plan.targets.size() + plan.missing.size() < limit) {
            BlockPos pos = queue.removeFirst();
            if (!seen.add(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!Matching.matches(settings.match(), target.getBlock(), state.getBlock()) || !Rules.exposed(level, pos, face)
                    || !Rules.removable(level, player, pos, state, exchange)) {
                continue;
            }
            if (!exchange) {
                plan.targets.add(new Plan.Target(pos, Plan.Kind.REMOVE, null, null, state));
            } else {
                BlockItem item = palette.take(rng, it -> it.getBlock() != state.getBlock() && swapState(level, player, hit, pos, it, state) != null);
                if (item != null) {
                    plan.targets.add(new Plan.Target(pos, Plan.Kind.SWAP, item, swapState(level, player, hit, pos, item, state), state));
                } else if (palette.first() != null && palette.first().getBlock() != state.getBlock()
                        && swapState(level, player, hit, pos, palette.first(), state) != null) {
                    plan.missing.add(pos);
                } else {
                    continue;
                }
            }
            spread(queue, pos, face, settings);
        }
        if (exchange && plan.targets.isEmpty() && !plan.missing.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_items");
        }
        return plan;
    }

    /** The state an Exchange puts in place of {@code old}: the new block, keeping the old block's orientation. */
    static @Nullable BlockState swapState(Level level, Player player, BlockHitResult hit, BlockPos pos, BlockItem item, BlockState old) {
        Block block = item.getBlock();
        if (Rules.denied(block.defaultBlockState())) {
            return null;
        }
        BlockState state = Placing.orient(block.defaultBlockState(), old);
        if (state.hasBlockEntity() || !state.canSurvive(level, pos)) {
            return null;
        }
        return state;
    }

    // Angel: one block behind the clicked block, or in mid-air in front of the player.

    private static Plan angel(Player player, Level level, @Nullable BlockHitResult hit, WandSettings settings, Supply supply, WandTier tier,
                              RandomSource rng) {
        Plan plan = new Plan(WandMode.ANGEL, 1);
        if (!player.isCreative() && !WandsConfig.server().angelWhileFalling && player.fallDistance > 10) {
            plan.problem = Component.translatable("reconstructedwands.problem.falling");
            return plan;
        }
        int reach = tier.stats().angel();
        BlockPos pos = null;
        BlockState against;
        BlockHitResult placeHit;
        Block target = null;
        if (hit != null) {
            BlockPos current = hit.getBlockPos();
            against = level.getBlockState(current);
            target = against.getBlock();
            for (int i = 0; i < reach; i++) {
                current = current.relative(hit.getDirection().getOpposite());
                if (Rules.placeable(level, player, current, settings.replace())) {
                    pos = current;
                    break;
                }
            }
            placeHit = hit;
        } else {
            Vec3 eye = player.getEyePosition();
            pos = BlockPos.containing(eye.add(player.getLookAngle().scale(2.0)));
            against = level.getBlockState(pos);
            placeHit = Placing.syntheticHit(pos, Direction.UP);
            if (!Rules.placeable(level, player, pos, settings.replace())) {
                pos = null;
            }
        }
        if (pos == null) {
            return plan;
        }
        Palette palette = Palette.of(player, settings, supply, target, true);
        if (palette.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_block");
            return plan;
        }
        BlockPos at = pos;
        BlockState copyFrom = hit != null && settings.direction() == WandSettings.Facing.TARGET ? against : null;
        BlockItem item = palette.take(rng, it -> Placing.stateFor(level, player, placeHit, at, it, copyFrom) != null);
        if (item != null) {
            plan.targets.add(new Plan.Target(at, Plan.Kind.PLACE, item, Placing.stateFor(level, player, placeHit, at, item, copyFrom), against));
        } else if (palette.first() != null && Placing.stateFor(level, player, placeHit, at, palette.first(), copyFrom) != null) {
            plan.missing.add(at);
            plan.problem = Component.translatable("reconstructedwands.problem.no_items");
        }
        return plan;
    }

    // Architect: a shape between the stored corner and the aimed-at position.

    private static Plan architect(Player player, Level level, @Nullable BlockHitResult hit, WandSettings settings, Supply supply, int limit,
                                  RandomSource rng) {
        Plan plan = new Plan(WandMode.ARCHITECT, limit);
        if (hit == null) {
            return plan;
        }
        BlockPos corner = hit.getBlockPos().relative(hit.getDirection());
        WandSettings.Spot anchor = settings.anchor();
        plan.corner = corner;
        if (anchor == null || !anchor.dimension().equals(Supply.dimensionId(level))) {
            // The click sets the first corner.
            return plan;
        }
        plan.anchor = anchor.pos();
        Shape shape = settings.shape();
        long count = shape.count(anchor.pos(), corner, limit);
        if (count < 0) {
            plan.problem = Component.translatable("reconstructedwands.problem.too_big", limit);
            return plan;
        }
        Palette palette = Palette.of(player, settings, supply, level.getBlockState(hit.getBlockPos()).getBlock(), true);
        if (palette.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_block");
            return plan;
        }
        Direction face = Direction.UP;
        for (BlockPos pos : shape.positions(anchor.pos(), corner)) {
            if (!Rules.placeable(level, player, pos, settings.replace())) {
                continue;
            }
            BlockHitResult placeHit = Placing.syntheticHit(pos, face);
            BlockItem item = palette.take(rng, it -> Placing.stateFor(level, player, placeHit, pos, it, null) != null);
            if (item != null) {
                plan.targets.add(new Plan.Target(pos, Plan.Kind.PLACE, item, Placing.stateFor(level, player, placeHit, pos, item, null),
                        level.getBlockState(pos.below())));
            } else if (palette.first() != null && Placing.stateFor(level, player, placeHit, pos, palette.first(), null) != null) {
                plan.missing.add(pos);
            }
        }
        if (plan.targets.isEmpty() && !plan.missing.isEmpty()) {
            plan.problem = Component.translatable("reconstructedwands.problem.no_items");
        }
        return plan;
    }

    // Shared flood-fill helpers (the 1.x neighbourhoods, diagonals included when both directions are free).

    private static boolean lockAllows(WandSettings settings, Direction face) {
        if (face.getAxis() == Direction.Axis.Y) {
            return settings.testLock(WandSettings.Lock.NORTHSOUTH) || settings.testLock(WandSettings.Lock.EASTWEST);
        }
        return settings.testLock(WandSettings.Lock.HORIZONTAL) || settings.testLock(WandSettings.Lock.VERTICAL);
    }

    private static void spread(Deque<BlockPos> queue, BlockPos pos, Direction face, WandSettings settings) {
        List<Direction> first = new ArrayList<>(2);
        List<Direction> second = new ArrayList<>(2);
        boolean a;
        boolean b;
        switch (face.getAxis()) {
            case Y -> {
                a = settings.testLock(WandSettings.Lock.NORTHSOUTH);
                b = settings.testLock(WandSettings.Lock.EASTWEST);
                first.add(Direction.NORTH);
                first.add(Direction.SOUTH);
                second.add(Direction.EAST);
                second.add(Direction.WEST);
            }
            case Z -> {
                a = settings.testLock(WandSettings.Lock.HORIZONTAL);
                b = settings.testLock(WandSettings.Lock.VERTICAL);
                first.add(Direction.EAST);
                first.add(Direction.WEST);
                second.add(Direction.UP);
                second.add(Direction.DOWN);
            }
            default -> {
                a = settings.testLock(WandSettings.Lock.HORIZONTAL);
                b = settings.testLock(WandSettings.Lock.VERTICAL);
                first.add(Direction.NORTH);
                first.add(Direction.SOUTH);
                second.add(Direction.UP);
                second.add(Direction.DOWN);
            }
        }
        if (a) {
            first.forEach(d -> queue.addLast(pos.relative(d)));
        }
        if (b) {
            second.forEach(d -> queue.addLast(pos.relative(d)));
        }
        if (a && b) {
            for (Direction d1 : first) {
                for (Direction d2 : second) {
                    queue.addLast(pos.relative(d1).relative(d2));
                }
            }
        }
    }
}
