package com.nightbeam.reconstructedwands.wand;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/** What a right-click does. Every mode but Construction comes from a core installed on the wand. */
public enum WandMode {
    CONSTRUCTION(null),
    ANGEL("core_angel"),
    DESTRUCTION("core_destruction"),
    EXCHANGE("core_exchange"),
    ARCHITECT("core_architect");

    private final @Nullable String core;

    WandMode(@Nullable String core) {
        this.core = core;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The registry path of the core item, or null for Construction. */
    public @Nullable String core() {
        return core;
    }

    public String translationKey() {
        return "reconstructedwands.mode." + key();
    }

    /** Whether the mode removes the blocks it touches (Destruction, Exchange). */
    public boolean removes() {
        return this == DESTRUCTION || this == EXCHANGE;
    }

    public static @Nullable WandMode byCore(String corePath) {
        for (WandMode mode : values()) {
            if (corePath.equals(mode.core)) {
                return mode;
            }
        }
        return null;
    }
}
