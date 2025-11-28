package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public record ResearchParchmentData(
        String knowledgeKey,
        String customName
) {
    public static final Codec<ResearchParchmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_key").forGetter(ResearchParchmentData::knowledgeKey),
            Codec.STRING.fieldOf("custom_name").forGetter(ResearchParchmentData::customName)
    ).apply(instance, ResearchParchmentData::new));

    public static ResearchParchmentData load(ItemStack itemStack) {
        CompoundTag itemTag = itemStack.getTag();
        if (itemTag != null && itemTag.contains("research_parchment_record")) {
            CompoundTag rpTag = itemTag.getCompound("research_parchment_record");
            DataResult<ResearchParchmentData> result = ResearchParchmentData.CODEC.decode(NbtOps.INSTANCE, rpTag).map(pair -> pair.getFirst());
            return result.resultOrPartial(CuriosityMod.LOGGER::warn).orElse(null);
        }
        return null;
    }

    public static void save(ItemStack itemStack, ResearchParchmentData rpRecord) {
        ResearchParchmentData.CODEC.encodeStart(NbtOps.INSTANCE, rpRecord).resultOrPartial(CuriosityMod.LOGGER::warn).ifPresent(tag -> {
            CompoundTag itemTag = itemStack.getOrCreateTag();
            itemTag.put("research_parchment_record", tag);
        });
    }
}
