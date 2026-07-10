package com.nigthbeam.reconstructedwands.basics;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigClient {
    public static final ModConfigSpec SPEC;
    public static final ClientConfig CLIENT;

    public static final ModConfigSpec.IntValue OPT_KEY;
    public static final ModConfigSpec.BooleanValue SHIFTOPT_MODE;
    public static final ModConfigSpec.BooleanValue SHIFTOPT_GUI;

    static {
        Pair<ClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        // Expose static fields for easy access, delegating to the instance
        OPT_KEY = CLIENT.optKey;
        SHIFTOPT_MODE = CLIENT.shiftOptMode;
        SHIFTOPT_GUI = CLIENT.shiftOptGui;
    }

    public static class ClientConfig {
        public final ModConfigSpec.IntValue optKey;
        public final ModConfigSpec.BooleanValue shiftOptMode;
        public final ModConfigSpec.BooleanValue shiftOptGui;

        public ClientConfig(ModConfigSpec.Builder builder) {
            builder.comment("This is the Client config for ConstructionWand.",
                    "If you're not familiar with NeoForge's new split client/server config, let me explain:",
                    "Client config is stored in the /config folder and only contains client specific settings like graphics and keybinds.",
                    "Mod behavior is configured in the Server config, which is world-specific and thus located",
                    "in the /saves/myworld/serverconfig folder. If you want to change the serverconfig for all",
                    "new worlds, copy the config files in the /defaultconfigs folder.");

            builder.push("keys");
            builder.comment(
                    "Key code of OPTKEY (Default: Left Control). Look up key codes under https://www.glfw.org/docs/3.3/group__keys.html");
            optKey = builder.defineInRange("OptKey", 341, 0, 350);
            builder.comment("Press SNEAK+OPTKEY instead of SNEAK for changing wand mode/direction lock");
            shiftOptMode = builder.define("ShiftOpt", false);
            builder.comment("Press SNEAK+OPTKEY instead of SNEAK for opening wand GUI");
            shiftOptGui = builder.define("ShiftOptGUI", false);
            builder.pop();
        }
    }
}
