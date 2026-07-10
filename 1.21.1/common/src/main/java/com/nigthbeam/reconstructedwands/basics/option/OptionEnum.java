package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.nbt.CompoundTag;
import com.nigthbeam.reconstructedwands.Constants;

/**
 * Enum option for wand configuration.
 */
public class OptionEnum<E extends Enum<E>> implements IOption<E> {
    private final String key;
    private E value;
    private final E defaultValue;
    private final E[] values;
    private boolean enabled;

    public OptionEnum(String key, E defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
        this.enabled = true;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValueString() {
        return value.name();
    }

    @Override
    public void setValueString(String val) {
        for (E v : values) {
            if (v.name().equals(val)) {
                value = v;
                return;
            }
        }
    }

    @Override
    public E get() {
        return value;
    }

    @Override
    public void set(E value) {
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
        int ord = value.ordinal();
        if (reverse) {
            ord = ord == 0 ? values.length - 1 : ord - 1;
        } else {
            ord = (ord + 1) % values.length;
        }
        value = values[ord];
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
        return Constants.MOD_ID + ".option." + key + "." + value.name().toLowerCase();
    }

    @Override
    public String getDescTranslation() {
        return Constants.MOD_ID + ".option." + key + ".desc";
    }

    @Override
    public void fromNBT(CompoundTag nbt) {
        if (nbt.contains(key)) {
            int ord = nbt.getInt(key);
            if (ord >= 0 && ord < values.length) {
                value = values[ord];
            } else {
                value = defaultValue;
            }
        } else {
            value = defaultValue;
        }
    }

    @Override
    public void toNBT(CompoundTag nbt) {
        nbt.putInt(key, value.ordinal());
    }
}
