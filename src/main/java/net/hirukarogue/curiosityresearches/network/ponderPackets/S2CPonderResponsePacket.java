package net.hirukarogue.curiosityresearches.network.ponderPackets;

import net.minecraft.client.Minecraft;

public class S2CPonderResponsePacket {
    private final String message;

    public S2CPonderResponsePacket(String message) {
        this.message = message;
    }

    public S2CPonderResponsePacket(net.minecraft.network.FriendlyByteBuf buffer) {
        this.message = buffer.readUtf(32767);
    }

    public void encode(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeUtf(message);
    }

    public void handle(java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.ResearchMenuScreen researchMenuScreen) {
                researchMenuScreen.setPonderMessage(message);
            }
        });
        context.setPacketHandled(true);
    }
}
