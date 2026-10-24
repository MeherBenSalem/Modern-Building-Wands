package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Which blocks count as "the same" for the Match setting. Groups come from {@code similar_blocks} in server.json. */
public final class Matching {
    private static Map<Block, Set<Block>> groups = Map.of();
    private static int builtFor = -1;

    private Matching() {
    }

    public static boolean matches(WandSettings.Match mode, Block a, Block b) {
        if (a == Blocks.AIR || b == Blocks.AIR) {
            return false;
        }
        return switch (mode) {
            case EXACT -> a == b;
            case SIMILAR -> a == b || similar(a).contains(b);
            case ANY -> true;
        };
    }

    /** The other blocks in {@code block}'s group (empty if it has none). */
    public static Set<Block> similar(Block block) {
        rebuildIfNeeded();
        return groups.getOrDefault(block, Set.of());
    }

    private static synchronized void rebuildIfNeeded() {
        if (builtFor == WandsConfig.generation()) {
            return;
        }
        Map<Block, Set<Block>> built = new HashMap<>();
        for (List<String> group : WandsConfig.server().similarBlocks) {
            Set<Block> blocks = new HashSet<>();
            for (String id : group) {
                ResourceLocation key = ResourceLocation.tryParse(id);
                Optional<Block> block = key == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(key);
                if (block.isPresent() && block.get() != Blocks.AIR) {
                    blocks.add(block.get());
                } else {
                    ReconstructedWands.LOG.warn("similar_blocks: unknown block {}", id);
                }
            }
            for (Block block : blocks) {
                Set<Block> others = built.computeIfAbsent(block, b -> new HashSet<>());
                others.addAll(blocks);
                others.remove(block);
            }
        }
        groups = built;
        builtFor = WandsConfig.generation();
    }
}
