package com.nightbeam.reconstructedwands.network;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.wand.Executor;
import com.nightbeam.reconstructedwands.wand.History;
import com.nightbeam.reconstructedwands.wand.Planner;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Server side of the network: handles client requests and sends the client what it needs to preview. */
public final class Network {
    private static final Map<UUID, Integer> LAST_SUPPLY = new HashMap<>();
    private static final Set<UUID> OPTION_DOWN = new HashSet<>();

    private Network() {
    }

    public static void handleSetting(ServerPlayer player, SettingPayload payload) {
        InteractionHand hand = wandHand(player);
        if (hand == null || payload.setting() < 0 || payload.setting() >= WandSettings.Setting.values().length) {
            return;
        }
        ItemStack wand = player.getItemInHand(hand);
        WandSettings.Setting setting = WandSettings.Setting.values()[payload.setting()];
        WandSettings settings = WandSettings.of(wand);
        int value = payload.value();
        if (setting == WandSettings.Setting.SIZE) {
            int max = Planner.maxSize(player, ((WandItem) wand.getItem()).tier(), settings.mode());
            value = value >= max ? 0 : value;
        }
        if (settings.apply(setting, value)) {
            settings.save();
        }
    }

    public static void handleCommand(ServerPlayer player, CommandPayload payload) {
        if (payload.command() == CommandPayload.OPTION_DOWN) {
            OPTION_DOWN.add(player.getUUID());
            return;
        }
        if (payload.command() == CommandPayload.OPTION_UP) {
            OPTION_DOWN.remove(player.getUUID());
            return;
        }
        InteractionHand hand = wandHand(player);
        if (hand == null) {
            return;
        }
        if (payload.command() == CommandPayload.UNDO) {
            Executor.undo(player);
        } else if (payload.command() == CommandPayload.REDO) {
            Executor.redo(player, player.getItemInHand(hand), hand);
        }
    }

    public static void sendHistory(ServerPlayer player) {
        History.Entry undo = History.peekUndo(player.getUUID());
        boolean sameLevel = undo != null && undo.dimension().equals(Supply.dimensionId(player.level()));
        ReconstructedWands.platform().sendToPlayer(player, new HistoryPayload(
                sameLevel ? new ArrayList<>(undo.positions()) : new ArrayList<>(), History.peekRedo(player.getUUID()) != null));
    }

    public static void sendConfig(ServerPlayer player) {
        ReconstructedWands.platform().sendToPlayer(player, new ConfigPayload(WandsConfig.serverJson()));
    }

    public static void sendConfigToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendConfig(player);
        }
    }

    /** Once a second: tell players holding a wand what their Ender Chest and bound container hold (only on change). */
    public static void tickSupply(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            InteractionHand hand = wandHand(player);
            if (hand == null) {
                continue;
            }
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (Map.Entry<Item, Integer> entry : Supply.remoteCounts(player, WandSettings.of(player.getItemInHand(hand))).entrySet()) {
                counts.put(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue());
            }
            int hash = counts.hashCode();
            Integer last = LAST_SUPPLY.put(player.getUUID(), hash);
            if (last == null || last != hash) {
                ReconstructedWands.platform().sendToPlayer(player, new SupplyPayload(counts));
            }
        }
    }

    public static void onJoin(ServerPlayer player) {
        LAST_SUPPLY.remove(player.getUUID());
        sendConfig(player);
        sendHistory(player);
    }

    /** Whether the player holds the option key (sent by their client). */
    public static boolean optionDown(ServerPlayer player) {
        return OPTION_DOWN.contains(player.getUUID());
    }

    public static void onLeave(ServerPlayer player) {
        LAST_SUPPLY.remove(player.getUUID());
        OPTION_DOWN.remove(player.getUUID());
        History.forget(player.getUUID());
    }

    /** The hand holding a wand, main hand first. */
    public static @Nullable InteractionHand wandHand(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof WandItem) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().getItem() instanceof WandItem) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
