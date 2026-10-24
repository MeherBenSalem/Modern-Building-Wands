package com.nightbeam.reconstructedwands.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightbeam.reconstructedwands.client.WandsClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reads Ctrl+Z / Ctrl+Y from the key event itself, so a quick tap of the shortcut is never missed between ticks. */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void reconstructedwands$shortcuts(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (action == InputConstants.PRESS) {
            WandsClient.onKeyPress(event, event.hasControlDown());
        }
    }
}
