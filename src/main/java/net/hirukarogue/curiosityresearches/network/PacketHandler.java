package net.hirukarogue.curiosityresearches.network;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.network.ponderPackets.C2SPonderPacket;
import net.hirukarogue.curiosityresearches.network.ponderPackets.S2CPonderResponsePacket;
import net.hirukarogue.curiosityresearches.network.researchPacket.C2SResearchPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
            new ResourceLocation(CuriosityMod.MOD_ID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;

        INSTANCE.messageBuilder(C2SPonderPacket.class, id++,
                NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SPonderPacket::encode)
                .decoder(C2SPonderPacket::new)
                .consumerMainThread(C2SPonderPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CPonderResponsePacket.class, id++,
                NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPonderResponsePacket::encode)
                .decoder(S2CPonderResponsePacket::new)
                .consumerMainThread(S2CPonderResponsePacket::handle)
                .add();

        INSTANCE.messageBuilder(C2SResearchPacket.class, id++,
                NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SResearchPacket::encode)
                .decoder(C2SResearchPacket::new)
                .consumerMainThread(C2SResearchPacket::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllClients(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
