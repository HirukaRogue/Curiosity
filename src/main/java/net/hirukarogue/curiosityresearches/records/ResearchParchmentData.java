package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ResearchParchmentData(
        String knowledgeKey,
        String customName,
        String tier
) {
    public static final Codec<ResearchParchmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_key").forGetter(ResearchParchmentData::knowledgeKey),
            Codec.STRING.fieldOf("custom_name").forGetter(ResearchParchmentData::customName),
            Codec.STRING.fieldOf("tier").forGetter(ResearchParchmentData::tier)
    ).apply(instance, ResearchParchmentData::new));
}
