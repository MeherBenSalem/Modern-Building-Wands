package com.nightbeam.reconstructedwands.neoforge;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.WandsEvents;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.network.CommandPayload;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.Network;
import com.nightbeam.reconstructedwands.network.SettingPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import com.nightbeam.reconstructedwands.platform.Platform;
import com.nightbeam.reconstructedwands.platform.Registrar;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ReconstructedWands.MOD_ID)
public final class ReconstructedWandsNeoForge {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    public ReconstructedWandsNeoForge(IEventBus modBus) {
        ReconstructedWands.init(new NeoForgePlatform(), new NeoForgeRegistrar());
        DeferredRegister<CreativeModeTab> tabs = register(Registries.CREATIVE_MODE_TAB);
        tabs.register(ModContent.tabId().getPath(), () -> CreativeModeTab.builder()
                .title(ModContent.tabTitle())
                .icon(ModContent::tabIcon)
                .displayItems((parameters, output) -> ModContent.forEachTabItem(output::accept))
                .build());
        registers.values().forEach(deferred -> deferred.register(modBus));

        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WandsEvents.onPlayerJoin(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WandsEvents.onPlayerLeave(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> WandsEvents.onServerTick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> WandsEvents.onServerStopped(event.getServer()));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> WandsEvents.registerCommands(event.getDispatcher()));
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> register(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, ReconstructedWands.MOD_ID));
    }

    /** Set by the client entry point; the server never receives these payloads. */
    static Consumer<CustomPacketPayload> clientReceiver = payload -> {
    };

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("2")
                .playToServer(SettingPayload.TYPE, SettingPayload.CODEC, (payload, context) -> Network.handleSetting((ServerPlayer) context.player(), payload))
                .playToServer(CommandPayload.TYPE, CommandPayload.CODEC, (payload, context) -> Network.handleCommand((ServerPlayer) context.player(), payload))
                .playToClient(HistoryPayload.TYPE, HistoryPayload.CODEC, (payload, context) -> clientReceiver.accept(payload))
                .playToClient(ConfigPayload.TYPE, ConfigPayload.CODEC, (payload, context) -> clientReceiver.accept(payload))
                .playToClient(SupplyPayload.TYPE, SupplyPayload.CODEC, (payload, context) -> clientReceiver.accept(payload));
    }

    private final class NeoForgeRegistrar implements Registrar {
        @Override
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<ResourceLocation, T> factory) {
            return ReconstructedWandsNeoForge.this.register(registry).register(path, factory);
        }
    }

    /** NeoForge's own events carry the protection: the break event before a removal, the place event after a placement. */
    private static final class NeoForgePlatform implements Platform {
        @Override
        public Path configDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            PacketDistributor.sendToPlayer(player, payload);
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            return player instanceof FakePlayer;
        }

        @Override
        public boolean isClient() {
            return FMLEnvironment.dist == Dist.CLIENT;
        }

        @Override
        public boolean mayPlace(ServerLevel level, BlockPos pos, ServerPlayer player) {
            return true;
        }

        @Override
        public boolean mayBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
            return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled();
        }

        @Override
        public boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockState against, ServerPlayer player) {
            BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
            if (!level.setBlock(pos, state, Block.UPDATE_ALL)) {
                return false;
            }
            if (NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(before, against, player)).isCanceled()) {
                before.restore(Block.UPDATE_ALL);
                return false;
            }
            return true;
        }

        @Override
        public void afterBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        }
    }
}
