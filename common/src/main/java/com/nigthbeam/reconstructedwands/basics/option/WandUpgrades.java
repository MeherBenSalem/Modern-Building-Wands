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
        ListTag listTag = new ListTag();
        try {
            Object val = tag.getList(key);
            if (val instanceof ListTag) {
                listTag = (ListTag) val;
            } else if (val instanceof java.util.Optional) {
                listTag = ((java.util.Optional<ListTag>) val).orElse(new ListTag());
            } else {
                listTag = new ListTag();
            }
        } catch (Exception e) {
            // Use empty list
        }
        boolean requireFix = false;

        for (int i = 0; i < listTag.size(); i++) {
            // Fix for Optional return type
            String str = "";
            try {
                Object val = listTag.getString(i);
                if (val instanceof String) {
                    str = (String) val;
                } else if (val instanceof java.util.Optional) {
                    str = ((java.util.Optional<String>) val).orElse("");
                } else {
                    // Fallback
                    try {
                        str = listTag.getString(i).toString(); // If it returned something else
                    } catch (Exception e2) {
                        str = "";
                    }
                }
            } catch (Exception e) {
                str = "";
            }

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
