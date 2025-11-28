package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SharedKnowledgeBookData(
        List<Knowledge> knowledges,
        String sharedBy
) {
    public static final Codec<SharedKnowledgeBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Knowledge.CODEC.listOf().fieldOf("knowledges").forGetter(SharedKnowledgeBookData::knowledges),
            Codec.STRING.fieldOf("shared_by").forGetter(SharedKnowledgeBookData::sharedBy)
    ).apply(instance, SharedKnowledgeBookData::new));

    public static SharedKnowledgeBookData load(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            if (itemStack.getTag().contains("shared_knowledge")) {
                CompoundTag kbTag = itemStack.getTag().getCompound("shared_knowledge");
                DataResult<SharedKnowledgeBookData> decode = SharedKnowledgeBookData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, kbTag);
                return decode.result().orElse(null);
            }
        }

        return null;
    }

    public static void save(ItemStack itemStack, SharedKnowledgeBookData sharedknowledgeRecord) {
        SharedKnowledgeBookData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, sharedknowledgeRecord).resultOrPartial(CuriosityMod.LOGGER::warn).ifPresent(tag -> {
            CompoundTag itemTag = itemStack.getOrCreateTag();
            itemTag.put("shared_knowledge", tag);
            itemStack.setTag(itemTag);
        });
    }
}
