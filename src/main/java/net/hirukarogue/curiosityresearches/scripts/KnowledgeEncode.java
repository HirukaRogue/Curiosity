package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record KnowledgeEncode(
        String key,
        String knowledgeName,
        String knowledgeDescription,
        List<ScriptElement> thingsToUnlock
) {
    /*private final String knowledgeName;
    private final String knowledgeDescription;
    private final List<ScriptElement> thingsToUnlock;*/

    /*private KnowledgeSubEncode(String knowledgeName, String knowledgeDescription, List<ScriptElement> thingsToUnlock) {
        this.knowledgeName = knowledgeName;
        this.knowledgeDescription = knowledgeDescription;
        this.thingsToUnlock = thingsToUnlock;
    }*/

    public static final Codec<KnowledgeEncode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(KnowledgeEncode::key),
            Codec.STRING.fieldOf("knowledge_name").forGetter(KnowledgeEncode::knowledgeName),
            Codec.STRING.optionalFieldOf("knowledge_description", "").forGetter(KnowledgeEncode::knowledgeDescription),
            Codec.list(ScriptElement.CODEC).optionalFieldOf("things_to_unlock", List.of()).forGetter(KnowledgeEncode::thingsToUnlock)
    ).apply(instance, KnowledgeEncode::new));

    /*public String getKnowledgeName() {
        return knowledgeName;
    }

    public String getKnowledgeDescription() {
        return knowledgeDescription;
    }

    public List<ScriptElement> getThingsToUnlock() {
        return thingsToUnlock;
    }*/
}
