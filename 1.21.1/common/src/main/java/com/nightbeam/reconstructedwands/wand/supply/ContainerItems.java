package com.nightbeam.reconstructedwands.wand.supply;

import com.nightbeam.reconstructedwands.config.ServerConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Reads and rewrites the contents of container items (shulker boxes and bundles) through their item data, so a
 * change is never made on a stale copy. This is the only class that knows this version's container components.
 */
final class ContainerItems {
    enum Kind { SHULKER_BOX, BUNDLE }

    private ContainerItems() {
    }

    static @Nullable Kind kind(ItemStack stack, ServerConfig config) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return null;
        }
        if (config.shulkerBoxes && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock
                && stack.has(DataComponents.CONTAINER)) {
            return Kind.SHULKER_BOX;
        }
        if (config.bundles && stack.has(DataComponents.BUNDLE_CONTENTS)) {
            return Kind.BUNDLE;
        }
        return null;
    }

    /** The contents, one entry per slot (shulker boxes keep empty slots so positions survive a rewrite). */
    static List<ItemStack> read(ItemStack container, Kind kind) {
        List<ItemStack> items = new ArrayList<>();
        if (kind == Kind.SHULKER_BOX) {
            NonNullList<ItemStack> slots = NonNullList.withSize(27, ItemStack.EMPTY);
            container.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(slots);
            items.addAll(slots);
        } else {
            container.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).itemCopyStream().forEach(items::add);
        }
        return items;
    }

    static void write(ItemStack container, Kind kind, List<ItemStack> items) {
        if (kind == Kind.SHULKER_BOX) {
            container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        } else {
            container.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items.stream().filter(s -> !s.isEmpty()).toList()));
        }
    }
}
