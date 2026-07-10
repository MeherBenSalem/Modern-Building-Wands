package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandCore;
import com.nigthbeam.reconstructedwands.api.IWandUpgrade;
import com.nigthbeam.reconstructedwands.platform.Services;

import java.util.ArrayList;

/**
 * Manages upgrades stored on a wand.
 */
public class WandUpgrades<T extends IWandUpgrade> {
    protected final CompoundTag tag;
    protected final String key;
    protected final ArrayList<T> upgrades;
    protected final T defaultValue;

    public WandUpgrades(CompoundTag tag, String key, T defaultValue) {
        this.tag = tag;
        this.key = key;
        this.defaultValue = defaultValue;

        upgrades = new ArrayList<>();
        if (defaultValue != null)
            upgrades.add(defaultValue);

        deserialize();
    }

    @SuppressWarnings("unchecked")
    protected void deserialize() {
        ListTag listTag = tag.getList(key, Tag.TAG_STRING);
        boolean requireFix = false;

        for (int i = 0; i < listTag.size(); i++) {
            String str = listTag.getString(i);
            Item item = Services.REGISTRY.getItem(ResourceLocation.parse(str));

            if (item == null) {
                Constants.LOG.warn("Invalid wand upgrade: {}", str);
                requireFix = true;
                continue;
            }

            try {
                T data = (T) item;
                upgrades.add(data);
            } catch (ClassCastException e) {
                Constants.LOG.warn("Invalid wand upgrade type: {}", str);
                requireFix = true;
            }
        }
        if (requireFix)
            serialize();
    }

    protected void serialize() {
        ListTag listTag = new ListTag();

        for (T item : upgrades) {
            if (item == defaultValue)
                continue;
            ResourceLocation key = getUpgradeKey(item);
            if (key != null) {
                listTag.add(StringTag.valueOf(key.toString()));
            }
        }
        tag.put(key, listTag);
    }

    protected ResourceLocation getUpgradeKey(T upgrade) {
        if (upgrade instanceof IWandCore core) {
            return core.getRegistryName();
        }
        if (upgrade instanceof Item item) {
            return Services.REGISTRY.getItemKey(item);
        }
        return null; // Should not happen for valid upgrades
    }

    public boolean addUpgrade(T upgrade) {
        if (hasUpgrade(upgrade))
            return false;

        upgrades.add(upgrade);
        serialize();
        return true;
    }

    public boolean hasUpgrade(T upgrade) {
        return upgrades.contains(upgrade);
    }

    public ArrayList<T> getUpgrades() {
        return upgrades;
    }
}
