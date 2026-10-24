package com.nightbeam.reconstructedwands.forge;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.WandsEvents;
import com.nightbeam.reconstructedwands.client.WandsClient;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.network.CommandPayload;
import com.nightbeam.reconstructedwands.network.ConfigPayload;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.network.Network;
import com.nightbeam.reconstructedwands.network.Payload;
import com.nightbeam.reconstructedwands.network.SettingPayload;
import com.nightbeam.reconstructedwands.network.SupplyPayload;
import com.nightbeam.reconstructedwands.platform.Platform;
import com.nightbeam.reconstructedwands.platform.Registrar;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;

/** The Forge 47 entry point. */
@Mod(ReconstructedWands.MOD_ID)
public final class ReconstructedWandsForge {
    /** Both sides must speak the same protocol, as with the 1.21 payload registrar. */
    private static final String PROTOCOL = "2";
    private static final Map<ResourceLocation, SimpleChannel> CHANNELS = new HashMap<>();

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    public ReconstructedWandsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ReconstructedWands.init(new ForgePlatform(), new ForgeRegistrar());
        DeferredRegister<CreativeModeTab> tabs = register(Registries.CREATIVE_MODE_TAB);
        tabs.register(ModContent.tabId().getPath(), () -> CreativeModeTab.builder()
                .title(ModContent.tabTitle())
                .icon(ModContent::tabIcon)
                .displayItems((parameters, output) -> ModContent.forEachTabItem(output::accept))
                .build());
        registers.values().forEach(deferred -> deferred.register(modBus));

        registerPayloads();
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WandsEvents.onPlayerJoin(player);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WandsEvents.onPlayerLeave(player);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                WandsEvents.onServerTick(event.getServer());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> WandsEvents.onServerStopped(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> WandsEvents.registerCommands(event.getDispatcher()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ReconstructedWandsForgeClient.init(modBus);
        }
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> register(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, ReconstructedWands.MOD_ID));
    }

    /**
     * One channel per payload, named like the 1.21 payload types ({@code reconstructedwands:setting} and so on). Client
     * handlers only run on the physical client.
     */
    private static void registerPayloads() {
        toServer(SettingPayload.ID, SettingPayload.class, SettingPayload::read, Network::handleSetting);
        toServer(CommandPayload.ID, CommandPayload.class, CommandPayload::read, Network::handleCommand);
        channel(HistoryPayload.ID).messageBuilder(HistoryPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HistoryPayload::write)
                .decoder(HistoryPayload::read)
                .consumerMainThread((payload, context) ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WandsClient.onHistory(payload)))
                .add();
        channel(ConfigPayload.ID).messageBuilder(ConfigPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ConfigPayload::write)
                .decoder(ConfigPayload::read)
                .consumerMainThread((payload, context) ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WandsClient.onConfig(payload)))
                .add();
        channel(SupplyPayload.ID).messageBuilder(SupplyPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SupplyPayload::write)
                .decoder(SupplyPayload::read)
                .consumerMainThread((payload, context) ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WandsClient.onSupply(payload)))
                .add();
    }

    private static <T extends Payload> void toServer(ResourceLocation id, Class<T> type, Function<FriendlyByteBuf, T> read,
                                                     BiConsumer<ServerPlayer, T> handler) {
        channel(id).messageBuilder(type, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Payload::write)
                .decoder(read)
                .consumerMainThread((T payload, Supplier<NetworkEvent.Context> context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        handler.accept(player, payload);
                    }
                })
                .add();
    }

    private static SimpleChannel channel(ResourceLocation id) {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(id, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        CHANNELS.put(id, channel);
        return channel;
    }

    /** Client to server, on the payload's own channel. */
    static void sendToServer(Payload payload) {
        CHANNELS.get(payload.id()).sendToServer(payload);
    }

    private final class ForgeRegistrar implements Registrar {
        @Override
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<ResourceLocation, T> factory) {
            ResourceLocation id = ReconstructedWands.id(path);
            return ReconstructedWandsForge.this.register(registry).register(path, () -> factory.apply(id));
        }
    }

    /** Forge's own events carry the protection: the break event before a removal, the place event after a placement. */
    private static final class ForgePlatform implements Platform {
        @Override
        public Path configDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public void sendToPlayer(ServerPlayer player, Payload payload) {
            CHANNELS.get(payload.id()).send(PacketDistributor.PLAYER.with(() -> player), payload);
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

        /** {@code post} returns true when a listener cancelled the event. */
        @Override
        public boolean mayBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
            return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player));
        }

        @Override
        public boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockState against, ServerPlayer player) {
            BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
            if (!level.setBlock(pos, state, Block.UPDATE_ALL)) {
                return false;
            }
            if (MinecraftForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(before, against, player))) {
                // As Forge itself rolls back a vetoed placement: the flag keeps the removed block from dropping anything.
                level.restoringBlockSnapshots = true;
                before.restore(true, true);
                level.restoringBlockSnapshots = false;
                return false;
            }
            return true;
        }

        @Override
        public void afterBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        }
    }
}
