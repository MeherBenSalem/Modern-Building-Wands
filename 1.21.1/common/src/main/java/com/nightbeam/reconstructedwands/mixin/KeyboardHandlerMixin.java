package com.nightbeam.reconstructedwands.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightbeam.reconstructedwands.client.WandsClient;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reads Ctrl+Z / Ctrl+Y from the key event itself, so a quick tap of the shortcut is never missed between ticks. */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void reconstructedwands$shortcuts(long handle, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (action == InputConstants.PRESS) {
            WandsClient.onKeyPress(key, scanCode, (modifiers & GLFW.GLFW_MOD_CONTROL) != 0);
        }
    }
}
