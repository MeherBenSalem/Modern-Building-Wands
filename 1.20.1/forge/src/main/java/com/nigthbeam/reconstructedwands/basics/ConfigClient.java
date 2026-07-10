package com.nigthbeam.reconstructedwands.basics;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigClient {
    public static final ForgeConfigSpec SPEC;
    public static final ClientConfig CLIENT;

    public static final ForgeConfigSpec.IntValue OPT_KEY;
    public static final ForgeConfigSpec.BooleanValue SHIFTOPT_MODE;
    public static final ForgeConfigSpec.BooleanValue SHIFTOPT_GUI;

    static {
        Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        // Expose static fields for easy access, delegating to the instance
        OPT_KEY = CLIENT.optKey;
        SHIFTOPT_MODE = CLIENT.shiftOptMode;
        SHIFTOPT_GUI = CLIENT.shiftOptGui;
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.IntValue optKey;
        public final ForgeConfigSpec.BooleanValue shiftOptMode;
        public final ForgeConfigSpec.BooleanValue shiftOptGui;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("This is the Client config for ConstructionWand.",
                    "Client config is stored separately from world-specific server config:",
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
