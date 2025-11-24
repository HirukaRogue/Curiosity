package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;

import java.util.List;

public record KnowledgeBookData(
        List<Knowledge> knowledges,
        String customName
) {
    public static final Codec<KnowledgeBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Knowledge.CODEC.listOf().fieldOf("knowledges").forGetter(KnowledgeBookData::knowledges),
            Codec.STRING.fieldOf("custom_name").forGetter(KnowledgeBookData::customName)
    ).apply(instance, KnowledgeBookData::new));
}
