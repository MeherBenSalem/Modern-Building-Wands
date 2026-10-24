package com.nightbeam.reconstructedwands.mixin;

import com.nightbeam.reconstructedwands.client.WandsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ctrl + scroll and Sneak + scroll with a wand in hand change its size and lock instead of the hotbar slot. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void reconstructedwands$scroll(long handle, double xOffset, double yOffset, CallbackInfo ci) {
        if (handle == minecraft.getWindow().handle() && minecraft.gui.screen() == null && minecraft.player != null
                && WandsClient.onScroll(yOffset)) {
            ci.cancel();
        }
    }
}
