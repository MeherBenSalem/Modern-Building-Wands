package com.nigthbeam.reconstructedwands.basics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.platform.Services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registry for similar blocks that can be matched together.
 */
public class ReplacementRegistry {
    private static final HashSet<HashSet<Item>> replacements = new HashSet<>();

    // Default similar blocks config
    private static final String[] DEFAULT_SIMILAR_BLOCKS = {
            "minecraft:dirt;minecraft:grass_block;minecraft:coarse_dirt;minecraft:podzol;minecraft:mycelium;minecraft:farmland;minecraft:dirt_path;minecraft:rooted_dirt"
    };

    public static void init() {
        init(List.of(DEFAULT_SIMILAR_BLOCKS));
    }

    public static void init(List<String> similarBlocks) {
        replacements.clear();

        for (String key : similarBlocks) {
            HashSet<Item> set = new HashSet<>();

            for (String id : key.split(";")) {
                Item item = Services.REGISTRY.getItem(ResourceLocation.parse(id));
                if (item == null || item == Items.AIR) {
                    Constants.LOG.warn("Replacement Registry: Could not find item {}", id);
                    continue;
                }
                set.add(item);
            }
            if (!set.isEmpty())
                replacements.add(set);
        }
    }

    public static Set<Item> getMatchingSet(Item item) {
        HashSet<Item> res = new HashSet<>();

        for (HashSet<Item> set : replacements) {
            if (set.contains(item))
                res.addAll(set);
        }
        res.remove(item);
        return res;
    }

    public static boolean matchBlocks(Block b1, Block b2) {
        if (b1 == b2)
            return true;
        if (b1 == Blocks.AIR || b2 == Blocks.AIR)
            return false;

        for (HashSet<Item> set : replacements) {
            if (set.contains(b1.asItem()) && set.contains(b2.asItem()))
                return true;
        }
        return false;
    }
}
