package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandUpgrade;

/**
 * Selectable wand upgrades that implement IOption for cycling through available
 * upgrades.
 */
public class WandUpgradesSelectable<T extends IWandUpgrade> extends WandUpgrades<T> implements IOption<T> {
    private byte selector;

    public WandUpgradesSelectable(CompoundTag tag, String key, T defaultValue) {
        super(tag, key, defaultValue);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValueString() {
        T current = get();
        ResourceLocation key = getUpgradeKey(current);
        return key != null ? key.toString() : "";
    }

    @Override
    public void setValueString(String val) {
        ResourceLocation loc = ResourceLocation.tryParse(val);
        if (loc == null)
            return;
        for (T upgrade : upgrades) {
            ResourceLocation upgKey = getUpgradeKey(upgrade);
            if (upgKey != null && upgKey.equals(loc)) {
                set(upgrade);
                return;
            }
        }
    }

    @Override
    public T get() {
        fixSelector();
        return upgrades.get(selector);
    }

    @Override
    public void set(T value) {
        selector = (byte) upgrades.indexOf(value);
        fixSelector();
        serializeSelector();
    }

    @Override
    public void next() {
        next(false);
    }

    @Override
    public void next(boolean reverse) {
        if (reverse) {
            selector--;
            if (selector < 0)
                selector = (byte) (upgrades.size() - 1);
        } else {
            selector++;
            if (selector >= upgrades.size())
                selector = 0;
        }
        fixSelector();
        serializeSelector();
    }

    @Override
    public boolean isEnabled() {
        return upgrades.size() > 1;
    }

    @Override
    public String getKeyTranslation() {
        return Constants.MOD_ID + ".option." + key;
    }

    @Override
    public String getValueTranslation() {
        T current = get();
        ResourceLocation key = getUpgradeKey(current);
        return Constants.MOD_ID + ".option." + this.key + "." + (key != null ? key.toString() : "default");
    }

    @Override
    public String getDescTranslation() {
        return Constants.MOD_ID + ".option." + key + ".desc";
    }

    @Override
    public void fromNBT(CompoundTag nbt) {
        // Already handled in constructor via deserialize()
    }

    @Override
    public void toNBT(CompoundTag nbt) {
        serialize();
        serializeSelector();
    }

    private void fixSelector() {
        if (selector < 0 || selector >= upgrades.size())
            selector = 0;
    }

    @Override
    protected void deserialize() {
        super.deserialize();
        selector = tag.getByte(key + "_sel");
        fixSelector();
    }

    @Override
    protected void serialize() {
        super.serialize();
        serializeSelector();
    }

    private void serializeSelector() {
        tag.putByte(key + "_sel", selector);
    }
}
