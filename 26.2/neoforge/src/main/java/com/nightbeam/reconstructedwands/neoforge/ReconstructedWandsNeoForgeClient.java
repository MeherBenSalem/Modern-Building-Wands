package com.nightbeam.reconstructedwands.neoforge;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.client.Preview;
import com.nightbeam.reconstructedwands.client.WandHud;
import com.nightbeam.reconstructedwands.client.WandsClient;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ReconstructedWands.MOD_ID, dist = Dist.CLIENT)
public final class ReconstructedWandsNeoForgeClient {
    public ReconstructedWandsNeoForgeClient(IEventBus modBus) {
        WandsClient.init(ClientPacketDistributor::sendToServer);
        modBus.addListener((RegisterKeyMappingsEvent event) -> WandsClient.keys().forEach(event::register));
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.HOTBAR, ReconstructedWands.id("hud"), (graphics, delta) -> WandHud.render(graphics)));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> {
            event.register(HistoryPayload.TYPE, (payload, context) -> WandsClient.onHistory(payload));
            event.register(ConfigPayload.TYPE, (payload, context) -> WandsClient.onConfig(payload));
            event.register(SupplyPayload.TYPE, (payload, context) -> WandsClient.onSupply(payload));
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> WandsClient.tick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> WandsClient.onDisconnect());
        NeoForge.EVENT_BUS.addListener((SubmitCustomGeometryEvent event) ->
                Preview.submit(event.getSubmitNodeCollector(), event.getLevelRenderState().cameraRenderState.pos));
        // The preview draws the targeted block itself, so the vanilla outline is left out while it shows.
        NeoForge.EVENT_BUS.addListener((ExtractBlockOutlineRenderStateEvent event) -> {
            if (Preview.showing(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        });
    }
}
