package com.nightbeam.reconstructedwands.content;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    /** Never placed, removed or swapped by a wand. */
    public static final TagKey<Block> DENIED = TagKey.create(Registries.BLOCK, ReconstructedWands.id("denied"));
    /** Never removed by Destruction or Exchange (they may still be placed). */
    /** Denied blocks a creative-mode player may still use a wand on (barriers, light blocks, structure voids). */
    public static final TagKey<Block> CREATIVE_ONLY = TagKey.create(Registries.BLOCK, ReconstructedWands.id("creative_only"));
    public static final TagKey<Block> NO_DISMANTLE = TagKey.create(Registries.BLOCK, ReconstructedWands.id("no_dismantle"));

    private ModTags() {
    }
}
