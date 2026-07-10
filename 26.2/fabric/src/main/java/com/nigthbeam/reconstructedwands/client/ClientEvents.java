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
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Fabric client event handlers for wand mode switching.
 */
public class ClientEvents {
    private static boolean optPressed = false;
    private static final KeyMapping.Category WAND_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("reconstructedwands", "wand"));

    public static final KeyMapping WAND_GUI_KEY = new KeyMapping(
            "key.reconstructedwands.wand_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            WAND_CATEGORY);

    public static void register() {
        KeyMappingHelper.registerKeyMapping(WAND_GUI_KEY);

        // Register client tick event for key state tracking
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Player player = client.player;
            if (player == null)
                return;

            ItemStack wand = WandUtil.holdingWand(player);
            if (wand != null && WAND_GUI_KEY.consumeClick()) {
                Minecraft.getInstance().setScreenAndShow(new ScreenWand(wand));
            }

            if (wand == null)
                return;

            boolean optState = isOptKeyDown();
            if (optPressed != optState) {
                optPressed = optState;
                com.nigthbeam.reconstructedwands.network.ModMessages.sendToServer(
                        new com.nigthbeam.reconstructedwands.network.PacketQueryUndo(optPressed));
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (world.isClientSide()) {
                if (handleWandRightClick(player, stack)) {
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static boolean isKeyDown(int id) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), id);
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

        Minecraft.getInstance().setScreenAndShow(new ScreenWand(wand));
        return true;
    }

    public static void openWandGUI(ItemStack wand) {
        Minecraft.getInstance().setScreenAndShow(new ScreenWand(wand));
    }
}
