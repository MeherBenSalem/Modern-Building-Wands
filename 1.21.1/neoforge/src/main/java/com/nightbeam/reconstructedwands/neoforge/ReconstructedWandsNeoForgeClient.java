package com.nightbeam.reconstructedwands.neoforge;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.client.Preview;
import com.nightbeam.reconstructedwands.client.WandHud;
import com.nightbeam.reconstructedwands.client.WandsClient;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = ReconstructedWands.MOD_ID, dist = Dist.CLIENT)
public final class ReconstructedWandsNeoForgeClient {
    public ReconstructedWandsNeoForgeClient(IEventBus modBus) {
        WandsClient.init(PacketDistributor::sendToServer);
        ReconstructedWandsNeoForge.clientReceiver = payload -> {
            if (payload instanceof HistoryPayload history) {
                WandsClient.onHistory(history);
            } else if (payload instanceof ConfigPayload config) {
                WandsClient.onConfig(config);
            } else if (payload instanceof SupplyPayload supply) {
                WandsClient.onSupply(supply);
            }
        };
        modBus.addListener((RegisterKeyMappingsEvent event) -> WandsClient.keys().forEach(event::register));
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.HOTBAR, ReconstructedWands.id("hud"), (graphics, delta) -> WandHud.render(graphics)));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> WandsClient.tick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> WandsClient.onDisconnect());
        // The preview draws the targeted block itself, so the vanilla outline is skipped while it shows.
        NeoForge.EVENT_BUS.addListener((RenderHighlightEvent.Block event) -> {
            if (Preview.showing(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        });
        // Drawn every frame, not only while a block is targeted: the Angel Core places in mid-air.
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                return;
            }
            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
            if (Preview.render(event.getPoseStack(), buffers, event.getCamera().getPosition())) {
                buffers.endBatch(RenderType.lines());
            }
        });
    }
}
