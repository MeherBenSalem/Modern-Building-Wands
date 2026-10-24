package com.nightbeam.reconstructedwands.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.reconstructedwands.ReconstructedWands;
import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;

/**
 * The loaded config files. On a client connected to a server, {@link #server()} is the server's copy (sent on join) so
 * previews and the HUD match what the server will do.
 */
public final class WandsConfig {
    private static final CommentedJson SERVER_SPEC = ServerConfig.spec();
    private static final CommentedJson CLIENT_SPEC = ClientConfig.spec();

    private static @Nullable Path dir;
    private static ServerConfig local = ServerConfig.from(SERVER_SPEC.defaults());
    private static JsonObject localJson = SERVER_SPEC.defaults();
    private static @Nullable ServerConfig synced;
    private static ClientConfig client = ClientConfig.from(CLIENT_SPEC.defaults());
    private static int generation;

    private WandsConfig() {
    }

    public static void load(Path configDir) {
        dir = configDir.resolve(ReconstructedWands.MOD_ID);
        reload();
    }

    /** Re-reads both files. Returns false if there is no config directory yet. */
    public static boolean reload() {
        if (dir == null) {
            return false;
        }
        localJson = SERVER_SPEC.load(dir.resolve("server.json"));
        local = ServerConfig.from(localJson);
        if (ReconstructedWands.platform() == null || ReconstructedWands.platform().isClient()) {
            client = ClientConfig.from(CLIENT_SPEC.load(dir.resolve("client.json")));
        }
        generation++;
        return true;
    }

    public static ServerConfig server() {
        return synced != null ? synced : local;
    }

    public static ClientConfig client() {
        return client;
    }

    /** Bumped whenever the effective server rules change, so caches (similar blocks, previews) can rebuild. */
    public static int generation() {
        return generation;
    }

    /** The local server.json as compact JSON, for the join sync. */
    public static String serverJson() {
        return localJson.toString();
    }

    /** Client side: adopt the server's rules. Bad JSON keeps the local rules. */
    public static void applySynced(String json) {
        try {
            JsonElement parsed = CommentedJson.parse(json);
            if (parsed != null && parsed.isJsonObject()) {
                synced = ServerConfig.from(CommentedJson.mergeOver(SERVER_SPEC.defaults(), parsed.getAsJsonObject()));
                generation++;
            }
        } catch (RuntimeException e) {
            ReconstructedWands.LOG.warn("Ignoring the server's wand config: {}", e.getMessage());
        }
    }

    /** Client side: back to the local rules after leaving a server. */
    public static void clearSynced() {
        if (synced != null) {
            synced = null;
            generation++;
        }
    }
}
