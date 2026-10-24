package com.nightbeam.reconstructedwands.wand;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Where a wand keeps its settings: the {@code wand_options} compound of the stack's custom data (as in 1.x). The tag
 * helpers keep the rest of the code free of this version's NBT API.
 */
public final class WandData {
    static final String ROOT = "wand_options";

    private WandData() {
    }

    public static CompoundTag read(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return root.getCompoundOrEmpty(ROOT);
    }

    public static void write(ItemStack stack, CompoundTag options) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put(ROOT, options);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    static int getInt(CompoundTag tag, String key, int fallback) {
        return tag.getIntOr(key, fallback);
    }

    static boolean getBool(CompoundTag tag, String key, boolean fallback) {
        return tag.getBooleanOr(key, fallback);
    }

    static byte getByte(CompoundTag tag, String key, byte fallback) {
        return tag.getByteOr(key, fallback);
    }

    static String getString(CompoundTag tag, String key, String fallback) {
        return tag.getStringOr(key, fallback);
    }

    static CompoundTag getCompound(CompoundTag tag, String key) {
        return tag.getCompoundOrEmpty(key);
    }

    static List<String> getStrings(CompoundTag tag, String key) {
        ListTag list = tag.getListOrEmpty(key);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String value = list.getStringOr(i, "");
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
