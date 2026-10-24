package com.nightbeam.reconstructedwands.fabric;

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
import eu.pb4.common.protection.api.CommonProtection;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ReconstructedWandsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ReconstructedWands.init(new FabricPlatform(), new FabricRegistrar());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModContent.tabId(),
                FabricCreativeModeTab.builder()
                        .title(ModContent.tabTitle())
                        .icon(ModContent::tabIcon)
                        .displayItems((parameters, output) -> ModContent.forEachTabItem(output::accept))
                        .build());

        PayloadTypeRegistry.serverboundPlay().register(SettingPayload.TYPE, SettingPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CommandPayload.TYPE, CommandPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HistoryPayload.TYPE, HistoryPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SupplyPayload.TYPE, SupplyPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SettingPayload.TYPE, (payload, context) -> Network.handleSetting(context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(CommandPayload.TYPE, (payload, context) -> Network.handleCommand(context.player(), payload));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> WandsEvents.onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> WandsEvents.onPlayerLeave(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(WandsEvents::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(WandsEvents::onServerStopped);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> WandsEvents.registerCommands(dispatcher));
    }

    private static final class FabricRegistrar implements Registrar {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<Identifier, T> factory) {
            Identifier id = ReconstructedWands.id(path);
            Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey) registry);
            T value = factory.apply(id);
            Registry.register(target, id, value);
            return () -> value;
        }
    }

    /** Fabric has no placement event, so claim mods are asked through the Common Protection API they implement. */
    private static final class FabricPlatform implements Platform {
        @Override
        public Path configDir() {
            return FabricLoader.getInstance().getConfigDir();
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            if (ServerPlayNetworking.canSend(player, payload.type())) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            return player instanceof FakePlayer;
        }

        @Override
        public boolean isClient() {
            return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        }

        @Override
        public boolean mayPlace(ServerLevel level, BlockPos pos, ServerPlayer player) {
            return CommonProtection.canPlaceBlock(level, pos, player.nameAndId(), player);
        }

        @Override
        public boolean mayBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
            return CommonProtection.canBreakBlock(level, pos, player.nameAndId(), player)
                    && PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, level.getBlockEntity(pos));
        }

        @Override
        public boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockState against, ServerPlayer player) {
            return level.setBlock(pos, state, Block.UPDATE_ALL);
        }

        @Override
        public void afterBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
            PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, null);
        }
    }
}
