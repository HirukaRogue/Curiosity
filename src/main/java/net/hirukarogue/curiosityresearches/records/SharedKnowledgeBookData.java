package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;

import java.util.List;

public record SharedKnowledgeBookData(
        List<Knowledge> knowledges,
        String sharedBy
) {
    public static final Codec<SharedKnowledgeBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Knowledge.CODEC.listOf().fieldOf("knowledges").forGetter(SharedKnowledgeBookData::knowledges),
            Codec.STRING.fieldOf("shared_by").forGetter(SharedKnowledgeBookData::sharedBy)
    ).apply(instance, SharedKnowledgeBookData::new));
}
