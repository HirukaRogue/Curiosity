package net.hirukarogue.curiosityresearches.network.ponderPackets;

import net.hirukarogue.curiosityresearches.network.PacketHandler;
import net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPonderPacket {
    private final BlockPos blockPos;

    public C2SPonderPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public C2SPonderPacket(FriendlyByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (player.level().getBlockEntity(blockPos) instanceof ResearchTableBlockEntity researchTable) {
                String ponderMessage = researchTable.ponder();
                PacketHandler.sendToPlayer(new S2CPonderResponsePacket(ponderMessage), player);
            }
        });

        context.setPacketHandled(true);
    }
}
