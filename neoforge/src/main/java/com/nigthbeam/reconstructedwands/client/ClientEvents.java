package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import com.nigthbeam.reconstructedwands.basics.ConfigClient;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.network.ModMessages;
import com.nigthbeam.reconstructedwands.network.PacketQueryUndo;
import com.nigthbeam.reconstructedwands.network.PacketWandOption;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {
    private boolean optPressed;

    // Keybind for opening wand GUI (default: G key)
    public static final KeyMapping WAND_GUI_KEY = new KeyMapping(
            "key.reconstructedwands.wand_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.reconstructedwands");

    public ClientEvents() {
        optPressed = false;
    }

    // Send state of OPT key to server and handle wand GUI keybind
    @SubscribeEvent
    public void KeyEvent(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack wand = WandUtil.holdingWand(player);

        // Handle wand GUI keybind
        if (event.getAction() == GLFW.GLFW_PRESS && wand != null) {
            if (WAND_GUI_KEY.matches(event.getKey(), event.getScanCode())) {
                Minecraft.getInstance().setScreen(new ScreenWand(wand));
                return;
            }
        }

        if (wand == null)
            return;

        boolean optState = isOptKeyDown();
        if (optPressed != optState) {
            optPressed = optState;
            ModMessages.sendToServer(new PacketQueryUndo(optPressed));
            // ConstructionWand.LOGGER.debug("OPT key update: " + optPressed);
        }
    }

    // Sneak+(OPT)+Scroll to change direction lock
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void MouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        // double scroll = event.getScrollDeltaY();
        double scroll = 0; // TODO: Fix scroll delta API

        if (player == null || !modeKeyCombDown(player) || scroll == 0)
            return;

        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null)
            return;

        WandOptions wandOptions = new WandOptions(wand);
        wandOptions.lock.next(scroll < 0);
        ModMessages.sendToServer(new PacketWandOption(wandOptions.lock, true));
        event.setCanceled(true);
    }

    // Sneak+(OPT)+Left click wand to change core
    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();

        if (player == null || !modeKeyCombDown(player))
            return;

        ItemStack wand = event.getItemStack();
        if (!(wand.getItem() instanceof ItemWand))
            return;

        WandOptions wandOptions = new WandOptions(wand);
        wandOptions.cores.next();
        ModMessages.sendToServer(new PacketWandOption(wandOptions.cores, true));
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
}
