package net.hirukarogue.curiosityresearches.network.sharePacket;

import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSharePacket {
    private final BlockPos blockPos;
    private final Knowledge knowledge;

    public C2SSharePacket(BlockPos blockPos, Knowledge knowledge) {
        this.blockPos = blockPos;
        this.knowledge = knowledge;
    }

    public C2SSharePacket(FriendlyByteBuf friendlyByteBuf) {
        this.blockPos = friendlyByteBuf.readBlockPos();
        this.knowledge = Knowledge.CODEC.parse(NbtOps.INSTANCE, friendlyByteBuf.readNbt())
                .resultOrPartial(error -> {
                    System.err.println("Erro ao decodificar Knowledge: " + error);
                })
                .orElse(null);
    }

    public void encode(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        CompoundTag tag = (CompoundTag) Knowledge.CODEC.encodeStart(NbtOps.INSTANCE, knowledge)
                .resultOrPartial(error -> System.err.println("Erro ao codificar Knowledge: " + error))
                .orElse(new CompoundTag());
        buffer.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (player.level().getBlockEntity(blockPos) instanceof net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableBlockEntity researchTable) {
                researchTable.shareKnowledge(knowledge);
            }
        });
        context.setPacketHandled(true);
    }
}
