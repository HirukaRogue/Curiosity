package net.hirukarogue.curiosityresearches.records.Knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Knowledge(
        String knowledgeName,
        int level,
        String knowledgeDescription
) {
    public static final Codec<Knowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_name").forGetter(Knowledge::knowledgeName),
            Codec.INT.fieldOf("level").forGetter(l -> Math.max(l.level(), 1)),
            Codec.STRING.optionalFieldOf("knowledge_description", "").forGetter(Knowledge::knowledgeDescription)
    ).apply(instance, Knowledge::new));

    public String key() {
        return knowledgeName + "_" + level;
    }
}
