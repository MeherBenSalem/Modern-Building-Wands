package com.nightbeam.reconstructedwands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.network.Network;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /wands reload}: re-reads the config files and sends the new rules to everyone online. */
public final class WandsCommand {
    private WandsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wands")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reload").executes(context -> {
                    if (!WandsConfig.reload()) {
                        context.getSource().sendFailure(Component.translatable("reconstructedwands.command.reload_failed"));
                        return 0;
                    }
                    Network.sendConfigToAll(context.getSource().getServer());
                    context.getSource().sendSuccess(() -> Component.translatable("reconstructedwands.command.reloaded"), true);
                    return 1;
                })));
    }
}
