package com.nigthbeam.reconstructedwands.basics;

/**
 * Fabric client configuration - simple static defaults.
 * TODO: Implement proper Fabric config using Cloth Config or similar.
 */
public class ConfigClient {
    // Wrapper class to mimic NeoForge config API
    public static class SimpleValue<T> {
        private T value;

        public SimpleValue(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    // Key code for OPT key (Default: Left Control = 341)
    public static final SimpleValue<Integer> OPT_KEY = new SimpleValue<>(341);

    // Press SNEAK+OPTKEY instead of SNEAK for changing wand mode/direction lock
    public static final SimpleValue<Boolean> SHIFTOPT_MODE = new SimpleValue<>(false);

    // Press SNEAK+OPTKEY instead of SNEAK for opening wand GUI
    public static final SimpleValue<Boolean> SHIFTOPT_GUI = new SimpleValue<>(true);
}
