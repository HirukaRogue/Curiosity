package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record KnowledgeBookData(
        List<Knowledge> knowledges,
        String customName
) {
    public static final Codec<KnowledgeBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Knowledge.CODEC.listOf().fieldOf("knowledges").forGetter(KnowledgeBookData::knowledges),
            Codec.STRING.optionalFieldOf("custom_name").forGetter(cn -> cn.customName != null ? cn.customName.describeConstable() : java.util.Optional.empty())
    ).apply(instance, (knowledges, customName) -> new KnowledgeBookData(
            knowledges,
            customName.orElse(null)
    )));

    public static KnowledgeBookData load(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            if (itemStack.getTag().contains("KnowledgeBookRecord")) {
                CompoundTag kbTag = itemStack.getTag().getCompound("KnowledgeBookRecord");
                DataResult<KnowledgeBookData> decode = KnowledgeBookData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, kbTag);
                return decode.result().orElse(null);
            }
        }

        return null;
    }

    public static void save(ItemStack itemStack, KnowledgeBookData knowledgeBookData) {
        KnowledgeBookData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, knowledgeBookData).resultOrPartial(msg -> CuriosityMod.LOGGER.warn("Failed loading knowledge book encode: " + msg)).ifPresent(tag -> {
            CompoundTag itemTag = itemStack.getOrCreateTag();
            itemTag.put("KnowledgeBookRecord", tag);
        });
    }
}
