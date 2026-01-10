package com.nigthbeam.reconstructedwands.network;

import com.nigthbeam.reconstructedwands.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ModMessages {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(PacketWandOption.TYPE, PacketWandOption.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketWandOption.TYPE, PacketWandOption::handle);
        Constants.LOG.info("Registered Fabric networking messages");
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
