package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.nbt.CompoundTag;
import com.nigthbeam.reconstructedwands.Constants;

/**
 * Boolean option for wand configuration.
 */
public class OptionBoolean implements IOption<Boolean> {
    private final String key;
    private Boolean value;
    private final Boolean defaultValue;
    private boolean enabled;

    public OptionBoolean(String key, Boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.enabled = true;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValueString() {
        return value.toString();
    }

    @Override
    public void setValueString(String val) {
        value = Boolean.parseBoolean(val);
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void set(Boolean value) {
        this.value = value;
    }

    @Override
    public void next() {
        next(false);
    }

    @Override
    public void next(boolean reverse) {
        if (!enabled)
            return;
        value = !value;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getKeyTranslation() {
        return Constants.MOD_ID + ".option." + key;
    }

    @Override
    public String getValueTranslation() {
        return Constants.MOD_ID + ".option." + key + "." + (value ? "on" : "off");
    }

    @Override
    public String getDescTranslation() {
        return Constants.MOD_ID + ".option." + key + ".desc";
    }

    @Override
    public void fromNBT(CompoundTag nbt) {
        if (nbt.contains(key)) {
            value = nbt.getBoolean(key);
        } else {
            value = defaultValue;
        }
    }

    @Override
    public void toNBT(CompoundTag nbt) {
        nbt.putBoolean(key, value);
    }
}
