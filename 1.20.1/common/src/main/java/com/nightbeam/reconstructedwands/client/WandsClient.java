package com.nightbeam.reconstructedwands.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.network.CommandPayload;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.Payload;
import com.nightbeam.reconstructedwands.network.SettingPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import com.nightbeam.reconstructedwands.wand.Planner;
import com.nightbeam.reconstructedwands.wand.WandMode;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Client entry: keys, scroll gestures, the preview tick and the server's messages. */
public final class WandsClient {
    public static final String CATEGORY = "key.categories.reconstructedwands.wands";
    public static final KeyMapping WHEEL_KEY = new KeyMapping("key.reconstructedwands.wheel", InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping SCREEN_KEY = new KeyMapping("key.reconstructedwands.screen", InputConstants.KEY_G, CATEGORY);
    public static final KeyMapping UNDO_KEY = new KeyMapping("key.reconstructedwands.undo", InputConstants.KEY_Z, CATEGORY);
    public static final KeyMapping REDO_KEY = new KeyMapping("key.reconstructedwands.redo", InputConstants.KEY_Y, CATEGORY);

    private static Consumer<Payload> sender = payload -> {
    };
    private static boolean optionSent;

    private WandsClient() {
    }

    /** Called once by the loader's client entry point with its way of sending packets to the server. */
    public static void init(Consumer<Payload> packetSender) {
        sender = packetSender;
        ClientBridge.install(Screen::hasShiftDown, WandsClient::optionKeyDown,
                () -> WandsConfig.client().sneakOpensScreen, wand -> Minecraft.getInstance().setScreen(new WandScreen(wand)));
    }

    public static List<KeyMapping> keys() {
        return List.of(WHEEL_KEY, SCREEN_KEY, UNDO_KEY, REDO_KEY);
    }

    public static void send(Payload payload) {
        sender.accept(payload);
    }

    public static boolean optionKeyDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL);
    }

    public static boolean wheelKeyDown() {
        InputConstants.Key key = InputConstants.getKey(WHEEL_KEY.saveString());
        return key.getType() == InputConstants.Type.KEYSYM && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue());
    }

    /** End of every client tick. */
    public static void tick(Minecraft minecraft) {
        Preview.tick(minecraft);
        if (minecraft.player == null) {
            return;
        }
        ItemStack wand = Preview.heldWand(minecraft.player);
        boolean option = wand != null && optionKeyDown();
        if (option != optionSent) {
            optionSent = option;
            send(new CommandPayload(option ? CommandPayload.OPTION_DOWN : CommandPayload.OPTION_UP));
        }
        boolean free = minecraft.screen == null;
        while (SCREEN_KEY.consumeClick()) {
            if (wand != null && free) {
                minecraft.setScreen(new WandScreen(wand));
                free = false;
            }
        }
        // Undo and redo are handled when the key goes down (onKeyPress); drop the queued clicks.
        while (UNDO_KEY.consumeClick()) {
        }
        while (REDO_KEY.consumeClick()) {
        }
        while (WHEEL_KEY.consumeClick()) {
            if (wand != null && free && WandSettings.of(wand).modes().size() > 1) {
                minecraft.setScreen(new ModeWheelScreen(wand));
                free = false;
            }
        }
    }

    /** A key going down anywhere in the game: Ctrl + the undo or redo key, with a wand in hand and no screen open. */
    public static void onKeyPress(int key, int scanCode, boolean control) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!control || minecraft.player == null || minecraft.screen != null || !WandsConfig.client().undoShortcuts
                || Preview.heldWand(minecraft.player) == null) {
            return;
        }
        if (UNDO_KEY.matches(key, scanCode)) {
            send(new CommandPayload(CommandPayload.UNDO));
        } else if (REDO_KEY.matches(key, scanCode)) {
            send(new CommandPayload(CommandPayload.REDO));
        }
    }

    /**
     * A scroll in the world (no screen open). Ctrl + scroll changes the size, Sneak + scroll the lock (or the shape for
     * the Architect). Returns true if the scroll was used (the hotbar then stays put).
     */
    public static boolean onScroll(double amount) {
        Minecraft minecraft = Minecraft.getInstance();
        if (amount == 0 || minecraft.player == null || !WandsConfig.client().scrollControls) {
            return false;
        }
        ItemStack wand = Preview.heldWand(minecraft.player);
        if (wand == null) {
            return false;
        }
        WandSettings settings = WandSettings.of(wand);
        boolean back = amount < 0;
        if (optionKeyDown()) {
            WandMode mode = settings.mode();
            int max = Planner.maxSize(minecraft.player, ((WandItem) wand.getItem()).tier(), mode);
            int current = settings.size() <= 0 ? max : Math.min(settings.size(), max);
            int step = current >= 64 ? 8 : 1;
            int next = Math.max(1, Math.min(max, current + (back ? -step : step)));
            int value = next >= max ? 0 : next;
            change(settings, WandSettings.Setting.SIZE, value);
            minecraft.player.displayClientMessage(Component.translatable("reconstructedwands.message.size", next, max), true);
            return true;
        }
        if (minecraft.player.isShiftKeyDown()) {
            WandSettings.Setting setting = settings.mode() == WandMode.ARCHITECT ? WandSettings.Setting.SHAPE : WandSettings.Setting.LOCK;
            int value = settings.cycled(setting, back);
            change(settings, setting, value);
            minecraft.player.displayClientMessage(Component.translatable(setting.translationKey()).append(": ")
                    .append(Component.translatable(WandItem.valueKey(setting, value))), true);
            return true;
        }
        return false;
    }

    private static void change(WandSettings settings, WandSettings.Setting setting, int value) {
        if (settings.apply(setting, value)) {
            settings.save();
        }
        send(new SettingPayload(setting.ordinal(), value));
    }

    // Messages from the server

    public static void onHistory(HistoryPayload payload) {
        Preview.onHistory(payload);
    }

    public static void onConfig(ConfigPayload payload) {
        WandsConfig.applySynced(payload.json());
    }

    public static void onSupply(SupplyPayload payload) {
        Map<Item, Integer> counts = new HashMap<>();
        payload.counts().forEach((id, count) -> {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                BuiltInRegistries.ITEM.getOptional(key).ifPresent(item -> counts.put(item, count));
            }
        });
        Supply.setRemoteCounts(counts);
    }

    public static void onDisconnect() {
        WandsConfig.clearSynced();
        Supply.setRemoteCounts(Map.of());
        Preview.reset();
        optionSent = false;
    }
}
