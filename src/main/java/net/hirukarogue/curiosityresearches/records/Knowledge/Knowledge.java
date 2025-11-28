package net.hirukarogue.curiosityresearches.records.Knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record Knowledge(
        String knowledgeName,
        int level,
        List<String> knowledgeDescription
) {
    public static final Codec<Knowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_name").forGetter(Knowledge::knowledgeName),
            Codec.INT.fieldOf("level").forGetter(l -> Math.max(l.level(), 1)),
            Codec.STRING.listOf().optionalFieldOf("knowledge_description").forGetter(k -> Optional.of(k.knowledgeDescription))
    ).apply(instance, (name, level, description) -> new Knowledge(name, level, description.orElse(List.of()))));

    public String key() {
        return knowledgeName + "_" + level;
    }
}
