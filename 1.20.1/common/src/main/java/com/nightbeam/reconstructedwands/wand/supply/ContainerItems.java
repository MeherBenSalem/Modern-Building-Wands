package com.nightbeam.reconstructedwands.wand.supply;

import com.nightbeam.reconstructedwands.config.ServerConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Reads and rewrites the contents of container items (shulker boxes and bundles) through their item tag, so a change is
 * never made on a stale copy. This is the only class that knows this version's container tags: a shulker box keeps
 * {@code BlockEntityTag.Items} (with slot numbers), a bundle keeps {@code Items} (no slots, newest first).
 */
final class ContainerItems {
    enum Kind { SHULKER_BOX, BUNDLE }

    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String ITEMS = "Items";
    private static final int SHULKER_SLOTS = 27;

    private ContainerItems() {
    }

    static @Nullable Kind kind(ItemStack stack, ServerConfig config) {
        if (stack.isEmpty() || stack.getCount() != 1) {
            return null;
        }
        if (config.shulkerBoxes && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock) {
            return Kind.SHULKER_BOX;
        }
        if (config.bundles && stack.getItem() instanceof BundleItem) {
            return Kind.BUNDLE;
        }
        return null;
    }

    /** The contents, one entry per slot (shulker boxes keep empty slots so positions survive a rewrite). */
    static List<ItemStack> read(ItemStack container, Kind kind) {
        List<ItemStack> items = new ArrayList<>();
        if (kind == Kind.SHULKER_BOX) {
            NonNullList<ItemStack> slots = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
            CompoundTag blockEntity = container.getTagElement(BLOCK_ENTITY_TAG);
            if (blockEntity != null) {
                ContainerHelper.loadAllItems(blockEntity, slots);
            }
            items.addAll(slots);
        } else {
            CompoundTag tag = container.getTag();
            ListTag list = tag == null ? new ListTag() : tag.getList(ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        return items;
    }

    /** Writes {@code items} back; an emptied container loses its list, like one emptied by hand. */
    static void write(ItemStack container, Kind kind, List<ItemStack> items) {
        if (kind == Kind.SHULKER_BOX) {
            NonNullList<ItemStack> slots = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(items.size(), SHULKER_SLOTS); i++) {
                slots.set(i, items.get(i));
            }
            CompoundTag blockEntity = container.getOrCreateTagElement(BLOCK_ENTITY_TAG);
            blockEntity.remove(ITEMS);
            ContainerHelper.saveAllItems(blockEntity, slots, false);
            if (blockEntity.isEmpty()) {
                container.removeTagKey(BLOCK_ENTITY_TAG);
            }
        } else {
            ListTag list = new ListTag();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    list.add(stack.save(new CompoundTag()));
                }
            }
            if (list.isEmpty()) {
                container.removeTagKey(ITEMS);
            } else {
                container.getOrCreateTag().put(ITEMS, list);
            }
        }
    }
}
