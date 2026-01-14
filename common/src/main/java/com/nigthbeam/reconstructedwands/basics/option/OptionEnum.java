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
            // Fix for Optional return type
            int ord = 0;
            try {
                Object val = nbt.getInt(key);
                if (val instanceof Integer) {
                    ord = (Integer) val;
                } else if (val instanceof java.util.Optional) {
                    ord = ((java.util.Optional<Integer>) val).orElse(0);
                } else {
                    ord = defaultValue.ordinal();
                }
            } catch (Exception e) {
                // Fallback
            }

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
        nbt.put(key, net.minecraft.nbt.IntTag.valueOf(value.ordinal()));
    }
}
