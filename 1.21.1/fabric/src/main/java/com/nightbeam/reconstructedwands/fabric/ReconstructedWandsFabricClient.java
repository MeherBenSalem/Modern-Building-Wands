package com.nightbeam.reconstructedwands.fabric;

import com.nightbeam.reconstructedwands.client.Preview;
import com.nightbeam.reconstructedwands.client.WandHud;
import com.nightbeam.reconstructedwands.client.WandsClient;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class ReconstructedWandsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WandsClient.init(ClientPlayNetworking::send);
        WandsClient.keys().forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(WandsClient::tick);
        ClientPlayNetworking.registerGlobalReceiver(HistoryPayload.TYPE, (payload, context) -> WandsClient.onHistory(payload));
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) -> WandsClient.onConfig(payload));
        ClientPlayNetworking.registerGlobalReceiver(SupplyPayload.TYPE, (payload, context) -> WandsClient.onSupply(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> WandsClient.onDisconnect());
        HudRenderCallback.EVENT.register((graphics, delta) -> WandHud.render(graphics));
        // Drawn every frame, not only while a block is targeted: the Angel Core places in mid-air.
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
            if (Preview.render(context.matrixStack(), buffers, context.camera().getPosition())) {
                buffers.endBatch(RenderType.lines());
            }
        });
        // Returning false skips the vanilla outline: the preview draws the targeted block itself.
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> !Preview.showing(Minecraft.getInstance().player));
    }
}
