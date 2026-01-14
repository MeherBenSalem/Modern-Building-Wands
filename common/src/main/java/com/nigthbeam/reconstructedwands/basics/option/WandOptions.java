package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.api.IWandUpgrade;
import com.nigthbeam.reconstructedwands.basics.ReplacementRegistry;
import com.nigthbeam.reconstructedwands.items.core.CoreDefault;

import org.jetbrains.annotations.Nullable;

/**
 * Wand options stored on an ItemStack using Data Components (MC 1.21).
 */
public class WandOptions {
    private final ItemStack wandStack;
    public CompoundTag tag;

    private static final String TAG_ROOT = "wand_options";

    public enum LOCK {
        HORIZONTAL,
        VERTICAL,
        NORTHSOUTH,
        EASTWEST,
        NOLOCK
    }

    public enum DIRECTION {
        TARGET,
        PLAYER
    }

    public enum MATCH {
        EXACT,
        SIMILAR,
        ANY
    }

    public final WandUpgradesSelectable<IWandCore> cores;

    public final OptionEnum<LOCK> lock;
    public final OptionEnum<DIRECTION> direction;
    public final OptionBoolean replace;
    public final OptionEnum<MATCH> match;
    public final OptionBoolean random;

    public final IOption<?>[] allOptions;

    public WandOptions(ItemStack wandStack) {
        this.wandStack = wandStack;

        // MC 1.21: Use CustomData component instead of direct NBT access
        CustomData customData = wandStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();

        if (rootTag.contains(TAG_ROOT)) {
            // Fix for Optional return type
            try {
                Object val = rootTag.getCompound(TAG_ROOT);
                if (val instanceof CompoundTag) {
                    tag = (CompoundTag) val;
                } else if (val instanceof java.util.Optional) {
                    tag = ((java.util.Optional<CompoundTag>) val).orElse(new CompoundTag());
                } else {
                    tag = new CompoundTag();
                }
            } catch (Exception e) {
                tag = new CompoundTag();
            }
        } else {
            tag = new CompoundTag();
        }

        cores = new WandUpgradesSelectable<>(tag, "cores", CoreDefault.INSTANCE);

        lock = new OptionEnum<>("lock", LOCK.NOLOCK);
        lock.fromNBT(tag);

        direction = new OptionEnum<>("direction", DIRECTION.TARGET);
        direction.fromNBT(tag);

        replace = new OptionBoolean("replace", true);
        replace.fromNBT(tag);

        match = new OptionEnum<>("match", MATCH.SIMILAR);
        match.fromNBT(tag);

        random = new OptionBoolean("random", false);
        random.fromNBT(tag);

        allOptions = new IOption[] { cores, lock, direction, replace, match, random };
    }

    @Nullable
    public IOption<?> get(String key) {
        for (IOption<?> option : allOptions) {
            if (option.getKey().equals(key))
                return option;
        }
        return null;
    }

    public boolean testLock(LOCK l) {
        if (lock.get() == LOCK.NOLOCK)
            return true;
        return lock.get() == l;
    }

    public boolean matchBlocks(Block b1, Block b2) {
        return switch (match.get()) {
            case EXACT -> b1 == b2;
            case SIMILAR -> ReplacementRegistry.matchBlocks(b1, b2);
            case ANY -> b1 != Blocks.AIR && b2 != Blocks.AIR;
        };
    }

    public boolean hasUpgrade(IWandUpgrade upgrade) {
        if (upgrade instanceof IWandCore core)
            return cores.hasUpgrade(core);
        return false;
    }

    public boolean addUpgrade(IWandUpgrade upgrade) {
        if (upgrade instanceof IWandCore core)
            return cores.addUpgrade(core);
        return false;
    }

    /**
     * Save all options to the tag via Data Components.
     */
    public void save() {
        for (IOption<?> option : allOptions) {
            option.toNBT(tag);
        }
        cores.toNBT(tag);

        // Get or create root tag and set our options
        CustomData customData = wandStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag rootTag = customData.copyTag();
        rootTag.put(TAG_ROOT, tag);

        // Set the updated custom data back on the stack
        wandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));
    }
}
