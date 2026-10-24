package com.nightbeam.reconstructedwands.fabric;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.client.Preview;
import com.nightbeam.reconstructedwands.client.WandHud;
import com.nightbeam.reconstructedwands.client.WandsClient;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;

public final class ReconstructedWandsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WandsClient.init(ClientPlayNetworking::send);
        WandsClient.keys().forEach(KeyMappingHelper::registerKeyMapping);
        ClientTickEvents.END_CLIENT_TICK.register(WandsClient::tick);
        ClientPlayNetworking.registerGlobalReceiver(HistoryPayload.TYPE, (payload, context) -> WandsClient.onHistory(payload));
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) -> WandsClient.onConfig(payload));
        ClientPlayNetworking.registerGlobalReceiver(SupplyPayload.TYPE, (payload, context) -> WandsClient.onSupply(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> WandsClient.onDisconnect());
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ReconstructedWands.id("hud"), (graphics, delta) -> WandHud.render(graphics));
        LevelRenderEvents.COLLECT_SUBMITS.register(context ->
                Preview.submit(context.submitNodeCollector(), context.levelState().cameraRenderState.pos));
        // Returning false skips the vanilla outline: the preview draws the targeted block itself.
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outline) -> !Preview.showing(Minecraft.getInstance().player));
    }
}
