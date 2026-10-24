package com.nightbeam.reconstructedwands.wand;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Where a wand keeps its settings: the {@code wand_options} compound of the stack's tag (as in 1.x). The tag helpers
 * keep the rest of the code free of this version's NBT API.
 */
public final class WandData {
    static final String ROOT = "wand_options";

    private WandData() {
    }

    /** A copy of the options (empty if the stack has none); reading never adds a tag to the stack. */
    public static CompoundTag read(ItemStack stack) {
        CompoundTag root = stack.getTag();
        return root == null ? new CompoundTag() : root.getCompound(ROOT).copy();
    }

    public static void write(ItemStack stack, CompoundTag options) {
        stack.getOrCreateTag().put(ROOT, options.copy());
    }

    static int getInt(CompoundTag tag, String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    static boolean getBool(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    static byte getByte(CompoundTag tag, String key, byte fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getByte(key) : fallback;
    }

    static String getString(CompoundTag tag, String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    static CompoundTag getCompound(CompoundTag tag, String key) {
        return tag.getCompound(key);
    }

    static List<String> getStrings(CompoundTag tag, String key) {
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String value = list.getString(i);
            if (!value.isEmpty()) {
                out.add(value);
            }
        }
        return out;
    }

    static void putStrings(CompoundTag tag, String key, List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        tag.put(key, list);
    }
}
