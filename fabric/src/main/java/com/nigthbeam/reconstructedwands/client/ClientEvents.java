package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.nigthbeam.reconstructedwands.basics.ConfigClient;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResultHolder;
import org.lwjgl.glfw.GLFW;

/**
 * Fabric client event handlers for wand mode switching.
 */
public class ClientEvents {
    private static boolean optPressed = false;

    public static final KeyMapping WAND_GUI_KEY = new KeyMapping(
            "key.reconstructedwands.wand_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.reconstructedwands");

    public static void register() {
        KeyBindingHelper.registerKeyBinding(WAND_GUI_KEY);

        // Register client tick event for key state tracking
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Player player = client.player;
            if (player == null)
                return;

            ItemStack wand = WandUtil.holdingWand(player);
            if (wand != null && WAND_GUI_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new ScreenWand(wand));
            }

            if (wand == null)
                return;

            boolean optState = isOptKeyDown();
            if (optPressed != optState) {
                optPressed = optState;
                // TODO: Send PacketQueryUndo when Fabric networking is implemented
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (world.isClientSide) {
                if (handleWandRightClick(player, stack)) {
                    return InteractionResultHolder.success(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        });
    }

    private static boolean isKeyDown(int id) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), id);
    }

    public static boolean isOptKeyDown() {
        return isKeyDown(ConfigClient.OPT_KEY.get());
    }

    public static boolean modeKeyCombDown(Player player) {
        return player.isCrouching() && (isOptKeyDown() || !ConfigClient.SHIFTOPT_MODE.get());
    }

    public static boolean guiKeyCombDown(Player player) {
        return player.isCrouching() && (isOptKeyDown() || !ConfigClient.SHIFTOPT_GUI.get());
    }

    /**
     * Handle sneak+right-click to open wand options GUI.
     * Call this from mixin or appropriate hook.
     */
    public static boolean handleWandRightClick(Player player, ItemStack wand) {
        if (!(wand.getItem() instanceof ItemWand))
            return false;
        if (!guiKeyCombDown(player))
            return false;

        Minecraft.getInstance().setScreen(new ScreenWand(wand));
        return true;
    }
}
