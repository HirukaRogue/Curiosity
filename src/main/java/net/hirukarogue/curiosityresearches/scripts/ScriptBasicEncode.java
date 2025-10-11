package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;

public class ScriptBasicEncode {
    private String knowledgeName;
    private String knowledgeDescription;
    private String knowledgeTier;
    private List<ScriptElement> thingsToUnlock;

    private ScriptBasicEncode(String knowledgeName, String knowledgeDescription, String knowledgeTier, List<ScriptElement> thingsToUnlock) {
        this.knowledgeName = knowledgeName;
        this.knowledgeDescription = knowledgeDescription;
        this.knowledgeTier = knowledgeTier;
        this.thingsToUnlock = thingsToUnlock;
    }

    public static final Codec<ScriptBasicEncode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_name").forGetter(ScriptBasicEncode::getKnowledgeName),
            Codec.STRING.optionalFieldOf("knowledge_description", "").forGetter(ScriptBasicEncode::getKnowledgeDescription),
            Codec.STRING.flatXmap(s -> java.util.Set.of("common", "uncommon", "rare", "epic", "legendary", "MYTHIC").contains(s)
                            ? com.mojang.serialization.DataResult.success(s)
                            : com.mojang.serialization.DataResult.error(() -> "Invalid knowledge_tier: " + s),
                    DataResult::success).fieldOf("knowledge_tier").forGetter(ScriptBasicEncode::getKnowledgeTier),
            Codec.list(ScriptElement.CODEC).optionalFieldOf("things_to_unlock", List.of()).forGetter(ScriptBasicEncode::getThingsToUnlock)
    ).apply(instance, ScriptBasicEncode::new));

    public String getKnowledgeName() {
        return knowledgeName;
    }

    public String getKnowledgeDescription() {
        return knowledgeDescription;
    }

    public String getKnowledgeTier() {
        return knowledgeTier;
    }

    public List<ScriptElement> getThingsToUnlock() {
        return Objects.requireNonNullElse(thingsToUnlock, List.of());
    }
}
