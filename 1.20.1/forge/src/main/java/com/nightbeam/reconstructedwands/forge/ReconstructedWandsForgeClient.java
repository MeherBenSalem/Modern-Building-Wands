package com.nightbeam.reconstructedwands.forge;

import com.nightbeam.reconstructedwands.client.Preview;
import com.nightbeam.reconstructedwands.client.WandHud;
import com.nightbeam.reconstructedwands.client.WandsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Client setup. Forge 47 has no client-only {@code @Mod} entry point, so {@link ReconstructedWandsForge} calls
 * {@link #init} on the physical client; the payload handlers live with the channels there.
 */
public final class ReconstructedWandsForgeClient {
    private ReconstructedWandsForgeClient() {
    }

    public static void init(IEventBus modBus) {
        WandsClient.init(ReconstructedWandsForge::sendToServer);
        modBus.addListener((RegisterKeyMappingsEvent event) -> WandsClient.keys().forEach(event::register));
        modBus.addListener((RegisterGuiOverlaysEvent event) -> event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "hud",
                (gui, graphics, partialTick, width, height) -> WandHud.render(graphics)));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                WandsClient.tick(Minecraft.getInstance());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> WandsClient.onDisconnect());
        // The preview draws the targeted block itself, so the vanilla outline is skipped while it shows.
        MinecraftForge.EVENT_BUS.addListener((RenderHighlightEvent.Block event) -> {
            if (Preview.showing(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        });
        // Drawn every frame, not only while a block is targeted: the Angel Core places in mid-air.
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
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
