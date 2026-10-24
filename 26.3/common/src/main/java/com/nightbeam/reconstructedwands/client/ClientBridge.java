package com.nightbeam.reconstructedwands.client;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

/**
 * Lets common code (items, tooltips) reach client state without loading client classes on a dedicated server.
 * {@code WandsClient.init} installs the real hooks.
 */
public final class ClientBridge {
    private static BooleanSupplier shiftDown = () -> false;
    private static BooleanSupplier optionKeyDown = () -> false;
    private static BooleanSupplier sneakOpensScreen = () -> true;
    private static Consumer<ItemStack> openScreen = stack -> {
    };

    private ClientBridge() {
    }

    public static void install(BooleanSupplier shift, BooleanSupplier optionKey, BooleanSupplier sneakScreen, Consumer<ItemStack> screen) {
        shiftDown = shift;
        optionKeyDown = optionKey;
        sneakOpensScreen = sneakScreen;
        openScreen = screen;
    }

    public static boolean shiftDown() {
        return shiftDown.getAsBoolean();
    }

    /** The option key (Ctrl by default): with sneak + right-click it undoes. */
    public static boolean optionKeyDown() {
        return optionKeyDown.getAsBoolean();
    }

    public static boolean sneakOpensScreen() {
        return sneakOpensScreen.getAsBoolean();
    }

    public static void openScreen(ItemStack wand) {
        openScreen.accept(wand);
    }
}
