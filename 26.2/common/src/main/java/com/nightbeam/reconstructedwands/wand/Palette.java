package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * The blocks one click may place, and how many of each are still free in this plan. Without Random the first block
 * that fits is used (the offhand block, else the target block, then its similar blocks). With Random each hotbar slot
 * holding a block is one share, so three slots of stone and one of andesite build three-to-one.
 */
final class Palette {
    private final Map<BlockItem, Integer> weights = new LinkedHashMap<>();
    private final Map<Item, Integer> used = new HashMap<>();
    private final boolean random;
    private final Supply supply;

    private Palette(Supply supply, boolean random) {
        this.supply = supply;
        this.random = random;
    }

    /**
     * @param target the block the click extends (null when there is none), whose similar blocks join unless Match is
     *               Exact
     */
    static Palette of(Player player, WandSettings settings, Supply supply, @Nullable Block target, boolean hotbarFallback) {
        ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        boolean random = settings.random();
        Palette palette = new Palette(supply, random && !(offhand.getItem() instanceof BlockItem));
        if (offhand.getItem() instanceof BlockItem block) {
            palette.weights.put(block, 1);
            return palette;
        }
        if (palette.random) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                if (player.getInventory().getItem(i).getItem() instanceof BlockItem block) {
                    palette.weights.merge(block, 1, Integer::sum);
                }
            }
            return palette;
        }
        if (target != null && target.asItem() instanceof BlockItem block) {
            palette.weights.put(block, 1);
            if (settings.match() != WandSettings.Match.EXACT) {
                for (Block similar : Matching.similar(target)) {
                    if (similar.asItem() instanceof BlockItem other) {
                        palette.weights.putIfAbsent(other, 1);
                    }
                }
            }
        }
        if (palette.weights.isEmpty() && hotbarFallback) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                if (player.getInventory().getItem(i).getItem() instanceof BlockItem block) {
                    palette.weights.put(block, 1);
                    break;
                }
            }
        }
        return palette;
    }

    boolean isEmpty() {
        return weights.isEmpty();
    }

    /** The first block of the palette (for the HUD and for positions that cannot be afforded). */
    @Nullable BlockItem first() {
        return weights.isEmpty() ? null : weights.keySet().iterator().next();
    }

    private int free(BlockItem item) {
        long left = (long) supply.count(item) - used.getOrDefault(item, 0);
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, left));
    }

    /** Picks an affordable block that {@code fits}, reserving one of it; null if none is left that fits. */
    @Nullable BlockItem take(RandomSource rng, Predicate<BlockItem> fits) {
        List<BlockItem> order = new ArrayList<>();
        if (random) {
            Map<BlockItem, Integer> pool = new LinkedHashMap<>();
            weights.forEach((item, weight) -> {
                if (free(item) > 0) {
                    pool.put(item, weight);
                }
            });
            while (!pool.isEmpty()) {
                int total = pool.values().stream().mapToInt(Integer::intValue).sum();
                int roll = rng.nextInt(total);
                BlockItem picked = null;
                for (Map.Entry<BlockItem, Integer> entry : pool.entrySet()) {
                    roll -= entry.getValue();
                    if (roll < 0) {
                        picked = entry.getKey();
                        break;
                    }
                }
                order.add(picked);
                pool.remove(picked);
            }
        } else {
            for (BlockItem item : weights.keySet()) {
                if (free(item) > 0) {
                    order.add(item);
                }
            }
        }
        for (BlockItem item : order) {
            if (fits.test(item)) {
                used.merge(item, 1, Integer::sum);
                return item;
            }
        }
        return null;
    }
}
