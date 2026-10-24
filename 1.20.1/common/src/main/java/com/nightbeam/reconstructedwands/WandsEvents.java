package com.nightbeam.reconstructedwands;

import com.mojang.brigadier.CommandDispatcher;
import com.nightbeam.reconstructedwands.command.WandsCommand;
import com.nightbeam.reconstructedwands.network.Network;
import com.nightbeam.reconstructedwands.wand.History;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Loader-neutral handlers for the events each loader forwards. */
public final class WandsEvents {
    private WandsEvents() {
    }

    public static void onPlayerJoin(ServerPlayer player) {
        Network.onJoin(player);
    }

    public static void onPlayerLeave(ServerPlayer player) {
        Network.onLeave(player);
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 == 0) {
            Network.tickSupply(server);
        }
    }

    public static void onServerStopped(MinecraftServer server) {
        History.clear();
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        WandsCommand.register(dispatcher);
    }
}
