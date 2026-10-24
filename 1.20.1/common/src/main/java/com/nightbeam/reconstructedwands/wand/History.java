package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Each player's undo and redo stacks (server side, in memory; cleared when the player leaves). */
public final class History {
    /**
     * One changed position: the block before and after, the item the wand used (to hand back on undo) and the drops
     * the player received (to take back on undo).
     */
    public record Change(BlockPos pos, Plan.Kind kind, BlockState before, BlockState after, @Nullable Item consumed, List<ItemStack> drops) {
    }

    public record Entry(String dimension, List<Change> changes) {
        public Set<BlockPos> positions() {
            Set<BlockPos> out = new LinkedHashSet<>();
            for (Change change : changes) {
                out.add(change.pos());
            }
            return out;
        }
    }

    private static final class Stacks {
        final Deque<Entry> undo = new ArrayDeque<>();
        final Deque<Entry> redo = new ArrayDeque<>();
    }

    private static final Map<UUID, Stacks> PLAYERS = new HashMap<>();

    private History() {
    }

    /** Records a new action: it becomes the next undo, and the redo stack is cleared. */
    public static synchronized void record(UUID player, Entry entry) {
        int depth = WandsConfig.server().undoDepth;
        if (depth <= 0 || entry.changes().isEmpty()) {
            return;
        }
        Stacks stacks = PLAYERS.computeIfAbsent(player, id -> new Stacks());
        stacks.undo.push(entry);
        stacks.redo.clear();
        while (stacks.undo.size() > depth) {
            stacks.undo.removeLast();
        }
    }

    public static synchronized @Nullable Entry peekUndo(UUID player) {
        Stacks stacks = PLAYERS.get(player);
        return stacks == null ? null : stacks.undo.peek();
    }

    public static synchronized @Nullable Entry peekRedo(UUID player) {
        Stacks stacks = PLAYERS.get(player);
        return stacks == null ? null : stacks.redo.peek();
    }

    /** Moves the top undo entry to the redo stack (after it was undone). */
    public static synchronized void undone(UUID player) {
        Stacks stacks = PLAYERS.get(player);
        if (stacks != null && !stacks.undo.isEmpty()) {
            stacks.redo.push(stacks.undo.pop());
        }
    }

    /** Replaces the top redo entry by what redoing it actually changed, on the undo stack. */
    public static synchronized void redone(UUID player, Entry result) {
        Stacks stacks = PLAYERS.get(player);
        if (stacks == null || stacks.redo.isEmpty()) {
            return;
        }
        stacks.redo.pop();
        if (!result.changes().isEmpty()) {
            stacks.undo.push(result);
        }
    }

    public static synchronized void forget(UUID player) {
        PLAYERS.remove(player);
    }

    public static synchronized void clear() {
        PLAYERS.clear();
    }
}
