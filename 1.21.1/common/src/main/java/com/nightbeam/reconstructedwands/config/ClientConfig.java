package com.nightbeam.reconstructedwands.config;

import com.google.gson.JsonObject;

import static com.nightbeam.reconstructedwands.config.Json.obj;

/** {@code config/reconstructedwands/client.json}: options that only change your own game. */
public final class ClientConfig {
    public final boolean preview;
    public final int colorPlace;
    public final int colorRemove;
    public final int colorMissing;
    public final int colorUndo;
    public final boolean hud;
    public final boolean undoShortcuts;
    public final boolean scrollControls;
    public final boolean sneakOpensScreen;

    private ClientConfig(JsonObject o) {
        preview = Json.bool(o, "preview", true);
        colorPlace = color(Json.str(o, "colors.place", "#7FE8FF"), 0x7FE8FF);
        colorRemove = color(Json.str(o, "colors.remove", "#FF9A3C"), 0xFF9A3C);
        colorMissing = color(Json.str(o, "colors.missing", "#FF4D4D"), 0xFF4D4D);
        colorUndo = color(Json.str(o, "colors.undo", "#7CFF7C"), 0x7CFF7C);
        hud = Json.bool(o, "hud", true);
        undoShortcuts = Json.bool(o, "undo_shortcuts", true);
        scrollControls = Json.bool(o, "scroll_controls", true);
        sneakOpensScreen = Json.bool(o, "sneak_opens_screen", true);
    }

    public static ClientConfig from(JsonObject json) {
        return new ClientConfig(json);
    }

    static int color(String text, int fallback) {
        String hex = text == null ? "" : text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static CommentedJson spec() {
        JsonObject d = obj(
                "preview", true,
                "colors", obj("place", "#7FE8FF", "remove", "#FF9A3C", "missing", "#FF4D4D", "undo", "#7CFF7C"),
                "hud", true,
                "undo_shortcuts", true,
                "scroll_controls", true,
                "sneak_opens_screen", true);
        return new CommentedJson(d)
                .header("RE:Constructed Wands 2.0: client options. These only change your own game.")
                .comment("preview", "Outline the blocks a click would change.")
                .comment("colors", "Outline colours (#RRGGBB): blocks to place, blocks to remove or swap, blocks you are missing,\n"
                        + "and the last action while you hold Ctrl (undo).")
                .comment("hud", "Show the mode, size and blocks next to the hotbar while you hold a wand.")
                .comment("undo_shortcuts", "Ctrl+Z undoes and Ctrl+Y redoes your last wand action while you hold a wand.")
                .comment("scroll_controls", "Ctrl + scroll changes the size; Sneak + scroll changes the lock or the shape.")
                .comment("sneak_opens_screen", "Sneak + right-click the air opens the wand's options.");
    }
}
