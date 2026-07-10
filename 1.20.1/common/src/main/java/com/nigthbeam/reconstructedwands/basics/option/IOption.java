package com.nigthbeam.reconstructedwands.basics.option;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface for wand options that can be serialized to NBT.
 */
public interface IOption<T> {
    /**
     * Get the key used for NBT serialization.
     */
    String getKey();

    String getValueString();

    void setValueString(String val);

    /**
     * Get the current value.
     */
    T get();

    /**
     * Set the value.
     */
    void set(T value);

    /**
     * Cycle to the next value.
     */
    void next();

    /**
     * Cycle to the next value, optionally in reverse.
     */
    void next(boolean reverse);

    /**
     * Check if this option is enabled (can be changed).
     */
    boolean isEnabled();

    /**
     * Get the translation key for this option.
     */
    String getKeyTranslation();

    /**
     * Get the translation key for the current value.
     */
    String getValueTranslation();

    /**
     * Get the translation key for the option description.
     */
    String getDescTranslation();

    /**
     * Read value from NBT.
     */
    void fromNBT(CompoundTag nbt);

    /**
     * Write value to NBT.
     */
    void toNBT(CompoundTag nbt);
}
