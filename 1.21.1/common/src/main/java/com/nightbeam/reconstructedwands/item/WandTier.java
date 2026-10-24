package com.nightbeam.reconstructedwands.item;

import com.nightbeam.reconstructedwands.config.ServerConfig;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import java.util.Locale;

public enum WandTier {
    STONE(new ServerConfig.Tier(131, 27, 2, 4, 4, false)),
    IRON(new ServerConfig.Tier(250, 128, 4, 9, 9, true)),
    DIAMOND(new ServerConfig.Tier(1561, 1024, 16, 81, 81, true)),
    NETHERITE(new ServerConfig.Tier(2031, 2048, 32, 128, 128, true)),
    INFINITY(new ServerConfig.Tier(0, 2048, 64, 128, 128, true));

    private final ServerConfig.Tier defaults;

    WandTier(ServerConfig.Tier defaults) {
        this.defaults = defaults;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Registry path of the wand item. */
    public String itemId() {
        return key() + "_wand";
    }

    public ServerConfig.Tier defaults() {
        return defaults;
    }

    public ServerConfig.Tier stats() {
        return WandsConfig.server().tier(this);
    }
}
