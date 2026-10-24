package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.network.Network;
import com.nightbeam.reconstructedwands.platform.Platform;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Carries out plans on the server. For every position the protection hooks are asked first, the item is taken before
 * the block is set, and a vetoed placement hands the item back, so no path creates or destroys items.
 */
public final class Executor {
    private Executor() {
    }

    /** A right-click with a wand. Returns true if anything happened. */
    public static boolean use(ServerPlayer player, ServerLevel level, ItemStack wand, InteractionHand hand, @Nullable BlockHitResult hit) {
        WandSettings settings = WandSettings.of(wand);
        if (settings.mode() == WandMode.ARCHITECT) {
            WandSettings.Spot anchor = settings.anchor();
            boolean anchored = anchor != null && anchor.dimension().equals(Supply.dimensionId(level));
            if (hit == null) {
                if (anchor != null) {
                    settings.clearAnchor();
                    settings.save();
                    tell(player, Component.translatable("reconstructedwands.message.corner_cleared"));
                    return true;
                }
                return false;
            }
            if (!anchored) {
                BlockPos corner = hit.getBlockPos().relative(hit.getDirection());
                if (!Rules.modifiable(level, player, corner)) {
                    return false;
                }
                settings.setAnchor(corner, Supply.dimensionId(level));
                settings.save();
                level.playSound(null, corner, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
                tell(player, Component.translatable("reconstructedwands.message.corner_set", corner.getX(), corner.getY(), corner.getZ()));
                return true;
            }
        }

        Plan plan = Planner.plan(player, level, wand, hit, level.getRandom());
        if (plan.isEmpty()) {
            if (plan.problem != null) {
                tell(player, plan.problem.copy().withStyle(ChatFormatting.RED));
            }
            return false;
        }
        Supply supply = Supply.of(player, settings);
        List<History.Change> changes = apply(player, level, wand, hand, plan.targets, supply);
        if (changes.isEmpty()) {
            return false;
        }
        if (plan.mode == WandMode.ARCHITECT) {
            WandSettings after = WandSettings.of(wand);
            after.clearAnchor();
            after.save();
        }
        player.awardStat(Stats.ITEM_USED.get(wand.getItem()));
        finish(player, level, changes);
        History.record(player.getUUID(), new History.Entry(Supply.dimensionId(level), changes));
        Network.sendHistory(player);
        return true;
    }

    /** Applies targets in order and returns what actually changed. Stops when the items or the wand run out. */
    static List<History.Change> apply(ServerPlayer player, ServerLevel level, ItemStack wand, InteractionHand hand, List<Plan.Target> targets,
                                      Supply supply) {
        Platform platform = ReconstructedWands.platform();
        List<History.Change> changes = new ArrayList<>();
        boolean drops = WandsConfig.server().destructionDrops;
        ItemStack tool = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        for (Plan.Target target : targets) {
            if (wand.isEmpty()) {
                break;
            }
            BlockPos pos = target.pos();
            BlockState current = level.getBlockState(pos);
            switch (target.kind()) {
                case PLACE -> {
                    BlockItem item = target.item();
                    if (item == null || target.state() == null || !Rules.modifiable(level, player, pos)
                            || !(current.isAir() || current.canBeReplaced()) || current.hasBlockEntity()) {
                        continue;
                    }
                    BlockState state = Block.updateFromNeighbourShapes(target.state(), level, pos);
                    if (!state.canSurvive(level, pos) || Rules.blockedByEntity(level, state, pos) || !platform.mayPlace(level, pos, player)) {
                        continue;
                    }
                    if (!supply.take(item)) {
                        return changes;
                    }
                    if (!platform.placeBlock(level, pos, state, target.against(), player)) {
                        supply.give(new ItemStack(item));
                        continue;
                    }
                    item.getBlock().setPlacedBy(level, pos, level.getBlockState(pos), player, new ItemStack(item));
                    changes.add(new History.Change(pos, Plan.Kind.PLACE, current, level.getBlockState(pos), item, List.of()));
                }
                case REMOVE -> {
                    if (current.getBlock() != target.against().getBlock() || !Rules.removable(level, player, pos, current, false)
                            || !platform.mayBreak(level, pos, current, player)) {
                        continue;
                    }
                    List<ItemStack> got = drops ? dropsOf(level, pos, current, player, tool) : List.of();
                    if (!level.destroyBlock(pos, false, player)) {
                        continue;
                    }
                    platform.afterBreak(level, pos, current, player);
                    got.forEach(supply::give);
                    changes.add(new History.Change(pos, Plan.Kind.REMOVE, current, level.getBlockState(pos), null, got));
                }
                case SWAP -> {
                    BlockItem item = target.item();
                    if (item == null || target.state() == null || current.getBlock() != target.against().getBlock()
                            || !Rules.removable(level, player, pos, current, true) || !platform.mayBreak(level, pos, current, player)
                            || !platform.mayPlace(level, pos, player)) {
                        continue;
                    }
                    BlockState state = Block.updateFromNeighbourShapes(target.state(), level, pos);
                    if (!state.canSurvive(level, pos)) {
                        continue;
                    }
                    if (!supply.take(item)) {
                        return changes;
                    }
                    // The offhand holds the new block, so the old one comes back as itself rather than as loot.
                    List<ItemStack> got = drops ? asItem(current) : List.of();
                    if (!platform.placeBlock(level, pos, state, current, player)) {
                        supply.give(new ItemStack(item));
                        continue;
                    }
                    level.levelEvent(2001, pos, Block.getId(current));
                    platform.afterBreak(level, pos, current, player);
                    got.forEach(supply::give);
                    changes.add(new History.Change(pos, Plan.Kind.SWAP, current, level.getBlockState(pos), item, got));
                }
            }
            damage(player, wand, hand);
        }
        return changes;
    }

    /** Undoes the player's last action. Everything is checked first, so an undo happens completely or not at all. */
    public static boolean undo(ServerPlayer player) {
        History.Entry entry = History.peekUndo(player.getUUID());
        if (entry == null) {
            tell(player, Component.translatable("reconstructedwands.message.nothing_to_undo"));
            return false;
        }
        ServerLevel level = player.serverLevel();
        if (!entry.dimension().equals(Supply.dimensionId(level))) {
            tell(player, Component.translatable("reconstructedwands.message.other_dimension").withStyle(ChatFormatting.RED));
            return false;
        }
        Platform platform = ReconstructedWands.platform();
        List<ItemStack> takeBack = new ArrayList<>();
        for (History.Change change : entry.changes()) {
            BlockState now = level.getBlockState(change.pos());
            boolean ok = switch (change.kind()) {
                case PLACE -> now.getBlock() == change.after().getBlock() && Rules.modifiable(level, player, change.pos())
                        && platform.mayBreak(level, change.pos(), now, player);
                case REMOVE -> (now.isAir() || now.canBeReplaced()) && !now.hasBlockEntity() && Rules.modifiable(level, player, change.pos())
                        && !Rules.blockedByEntity(level, change.before(), change.pos()) && platform.mayPlace(level, change.pos(), player);
                case SWAP -> now.getBlock() == change.after().getBlock() && Rules.modifiable(level, player, change.pos())
                        && platform.mayBreak(level, change.pos(), now, player);
            };
            if (!ok) {
                tell(player, Component.translatable("reconstructedwands.message.undo_blocked", change.pos().getX(), change.pos().getY(),
                        change.pos().getZ()).withStyle(ChatFormatting.RED));
                return false;
            }
            takeBack.addAll(change.drops());
        }
        if (!Supply.has(player, takeBack)) {
            tell(player, Component.translatable("reconstructedwands.message.undo_needs_drops").withStyle(ChatFormatting.RED));
            return false;
        }
        Supply.takeBack(player, takeBack);
        List<History.Change> reversed = new ArrayList<>(entry.changes());
        java.util.Collections.reverse(reversed);
        for (History.Change change : reversed) {
            BlockPos pos = change.pos();
            switch (change.kind()) {
                case PLACE -> {
                    level.removeBlock(pos, false);
                    giveBack(player, change.consumed());
                }
                case REMOVE -> {
                    if (!platform.placeBlock(level, pos, change.before(), level.getBlockState(pos.below()), player)) {
                        change.drops().forEach(stack -> Supply.give(player, stack));
                    }
                }
                case SWAP -> {
                    level.setBlock(pos, change.before(), Block.UPDATE_ALL);
                    giveBack(player, change.consumed());
                }
            }
        }
        History.undone(player.getUUID());
        level.playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.2F);
        tell(player, Component.translatable("reconstructedwands.message.undone", entry.changes().size()));
        Network.sendHistory(player);
        return true;
    }

    /** Redoes the last undone action with the wand the player holds (it costs items and durability again). */
    public static boolean redo(ServerPlayer player, ItemStack wand, InteractionHand hand) {
        History.Entry entry = History.peekRedo(player.getUUID());
        if (entry == null) {
            tell(player, Component.translatable("reconstructedwands.message.nothing_to_redo"));
            return false;
        }
        ServerLevel level = player.serverLevel();
        if (!entry.dimension().equals(Supply.dimensionId(level))) {
            tell(player, Component.translatable("reconstructedwands.message.other_dimension").withStyle(ChatFormatting.RED));
            return false;
        }
        List<Plan.Target> targets = new ArrayList<>();
        for (History.Change change : entry.changes()) {
            BlockItem item = change.consumed() instanceof BlockItem block ? block : null;
            targets.add(new Plan.Target(change.pos(), change.kind(), item, change.after(), change.before()));
        }
        List<History.Change> changes = apply(player, level, wand, hand, targets, Supply.of(player, WandSettings.of(wand)));
        History.redone(player.getUUID(), new History.Entry(entry.dimension(), changes));
        if (changes.isEmpty()) {
            tell(player, Component.translatable("reconstructedwands.message.redo_failed").withStyle(ChatFormatting.RED));
            Network.sendHistory(player);
            return false;
        }
        finish(player, level, changes);
        tell(player, Component.translatable("reconstructedwands.message.redone", changes.size()));
        Network.sendHistory(player);
        return true;
    }

    private static void finish(ServerPlayer player, ServerLevel level, List<History.Change> changes) {
        History.Change first = changes.get(0);
        if (first.kind() != Plan.Kind.REMOVE) {
            SoundType sound = first.after().getSoundType();
            level.playSound(null, first.pos(), sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
                    sound.getPitch() * 0.8F);
        }
    }

    private static List<ItemStack> dropsOf(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, ItemStack tool) {
        BlockEntity entity = level.getBlockEntity(pos);
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack drop : Block.getDrops(state, level, pos, entity, player, tool)) {
            if (!drop.isEmpty()) {
                out.add(drop);
            }
        }
        return out;
    }

    private static List<ItemStack> asItem(BlockState state) {
        Item item = state.getBlock().asItem();
        return item instanceof BlockItem ? List.of(new ItemStack(item)) : List.of();
    }

    private static void giveBack(ServerPlayer player, @Nullable Item item) {
        if (item != null) {
            Supply.give(player, new ItemStack(item));
        }
    }

    private static void damage(ServerPlayer player, ItemStack wand, InteractionHand hand) {
        if (!player.isCreative() && wand.isDamageableItem()) {
            wand.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
    }

    static void tell(ServerPlayer player, Component message) {
        player.sendSystemMessage(message, true);
    }
}
