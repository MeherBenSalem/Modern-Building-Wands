package com.nightbeam.reconstructedwands.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.reconstructedwands.item.WandTier;
import com.nightbeam.reconstructedwands.wand.WandMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.nightbeam.reconstructedwands.config.Json.arr;
import static com.nightbeam.reconstructedwands.config.Json.obj;

/** {@code config/reconstructedwands/server.json}: the rules. The server's copy is sent to every client that joins. */
public final class ServerConfig {
    /** Per-tier numbers. A durability of 0 means unbreakable. */
    public record Tier(int durability, int construction, int angel, int destruction, int exchange, boolean cores) {
    }

    public final Map<WandTier, Tier> tiers = new EnumMap<>(WandTier.class);
    public final Map<WandMode, Boolean> modes = new EnumMap<>(WandMode.class);
    public final int creativeLimit;
    public final int maxRange;
    public final int undoDepth;
    public final boolean shulkerBoxes;
    public final boolean bundles;
    public final boolean enderChest;
    public final boolean boundContainers;
    public final int boundRange;
    public final boolean destructionDrops;
    public final boolean angelWhileFalling;
    public final List<List<String>> similarBlocks = new ArrayList<>();

    private ServerConfig(JsonObject o) {
        for (WandTier tier : WandTier.values()) {
            Tier d = tier.defaults();
            String p = "tiers." + tier.key() + ".";
            tiers.put(tier, new Tier(
                    clamp(Json.integer(o, p + "durability", d.durability()), 0, 1_000_000),
                    clamp(Json.integer(o, p + "construction", d.construction()), 1, 65_536),
                    clamp(Json.integer(o, p + "angel", d.angel()), 1, 256),
                    clamp(Json.integer(o, p + "destruction", d.destruction()), 1, 65_536),
                    clamp(Json.integer(o, p + "exchange", d.exchange()), 1, 65_536),
                    Json.bool(o, p + "cores", d.cores())));
        }
        for (WandMode mode : WandMode.values()) {
            modes.put(mode, mode == WandMode.CONSTRUCTION || Json.bool(o, "modes." + mode.key(), true));
        }
        creativeLimit = clamp(Json.integer(o, "creative_limit", 8192), 1, 65_536);
        maxRange = clamp(Json.integer(o, "max_range", 256), 0, 4096);
        undoDepth = clamp(Json.integer(o, "undo_depth", 10), 0, 100);
        shulkerBoxes = Json.bool(o, "sources.shulker_boxes", true);
        bundles = Json.bool(o, "sources.bundles", true);
        enderChest = Json.bool(o, "sources.ender_chest", true);
        boundContainers = Json.bool(o, "sources.bound_containers", true);
        boundRange = clamp(Json.integer(o, "sources.bound_range", 64), 1, 4096);
        destructionDrops = Json.bool(o, "destruction_drops", true);
        angelWhileFalling = Json.bool(o, "angel_while_falling", false);
        JsonElement groups = Json.at(o, "similar_blocks");
        if (groups != null && groups.isJsonArray()) {
            for (JsonElement group : groups.getAsJsonArray()) {
                if (group.isJsonArray()) {
                    List<String> ids = new ArrayList<>();
                    for (JsonElement id : group.getAsJsonArray()) {
                        if (id.isJsonPrimitive()) {
                            ids.add(id.getAsString());
                        }
                    }
                    if (ids.size() > 1) {
                        similarBlocks.add(ids);
                    }
                }
            }
        }
    }

    public static ServerConfig from(JsonObject json) {
        return new ServerConfig(json);
    }

    public Tier tier(WandTier tier) {
        return tiers.get(tier);
    }

    public boolean enabled(WandMode mode) {
        return modes.getOrDefault(mode, true);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static CommentedJson spec() {
        JsonObject tiers = new JsonObject();
        for (WandTier tier : WandTier.values()) {
            Tier d = tier.defaults();
            tiers.add(tier.key(), obj("durability", d.durability(), "construction", d.construction(), "angel", d.angel(),
                    "destruction", d.destruction(), "exchange", d.exchange(), "cores", d.cores()));
        }
        JsonObject modes = new JsonObject();
        for (WandMode mode : WandMode.values()) {
            if (mode != WandMode.CONSTRUCTION) {
                modes.addProperty(mode.key(), true);
            }
        }
        JsonArray similar = arr(arr("minecraft:dirt", "minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:podzol",
                "minecraft:mycelium", "minecraft:rooted_dirt", "minecraft:dirt_path", "minecraft:farmland"));
        JsonObject d = obj(
                "tiers", tiers,
                "creative_limit", 8192,
                "max_range", 256,
                "undo_depth", 10,
                "modes", modes,
                "sources", obj("shulker_boxes", true, "bundles", true, "ender_chest", true, "bound_containers", true, "bound_range", 64),
                "destruction_drops", true,
                "angel_while_falling", false,
                "similar_blocks", similar);
        return new CommentedJson(d)
                .header("RE:Constructed Wands 2.0: server rules. Clients get the server's copy when they join.\n"
                        + "Reload with /wands reload. Durability changes need a restart.")
                .comment("tiers", "Per wand: durability (0 = unbreakable), the most blocks one click may place (construction),\n"
                        + "how far the Angel Core reaches, and the most blocks Destruction and Exchange may touch.\n"
                        + "The Architect Core uses the construction number. cores: whether the wand accepts cores.")
                .comment("creative_limit", "The Infinity Wand's limit in creative mode.")
                .comment("max_range", "No block further than this from the player is ever changed (0 = no limit).")
                .comment("undo_depth", "Actions each player can undo (and redo). 0 turns undo off.")
                .comment("modes", "Turn off a core's mode. Wands keep the core but cannot use the mode.")
                .comment("sources", "Where wands find blocks besides loose stacks in the inventory. bound_range is how far a bound\n"
                        + "container may be (same dimension, loaded chunks only).")
                .comment("destruction_drops", "Destruction hands the removed blocks' drops to the player, as if broken by the tool in the\n"
                        + "offhand; Exchange hands back the swapped-out blocks themselves. false deletes them.")
                .comment("angel_while_falling", "Allow the Angel Core in survival while falling.")
                .comment("similar_blocks", "Groups of blocks that the \"Similar\" match setting treats as one block.");
    }
}
