package net.hirukarogue.curiosityresearches.miscellaneous.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class Knowledge {
    private final String knowledge_name;
    @Nullable
    private final String knowledge_description;
    @Nullable
    private final Unlocks unlocks;

    public Knowledge(String knowledge_name,@Nullable String knowledge_description, @Nullable Unlocks unlocks) {
        this.knowledge_name = knowledge_name;
        this.knowledge_description = knowledge_description;
        this.unlocks = unlocks;
    }

    public Knowledge(String knowledge_name, @Nullable String knowledge_description) {
        this(knowledge_name, knowledge_description, null);
    }

    public Knowledge(String knowledge_name, @Nullable Unlocks unlocks) {
        this(knowledge_name, null, unlocks);
    }

    public Knowledge(String knowledge_name) {
        this(knowledge_name, null, null);
    }

    public static Codec<Knowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_name").forGetter(Knowledge::getKnowledge_name),
            Codec.STRING.optionalFieldOf("knowledge_description").forGetter(knowledge -> Optional.ofNullable(knowledge.knowledge_description)),
            Unlocks.CODEC.optionalFieldOf("unlocks").forGetter(knowledge -> Optional.ofNullable(knowledge.unlocks))
    ).apply(instance, (knowledge_name, knowledge_description, unlocks) -> new Knowledge(knowledge_name, knowledge_description.orElse(null), unlocks.orElse(null))));

    public String getKnowledge_name() {
        return knowledge_name;
    }

    public String getKnowledge_description() {
        return Objects.requireNonNullElse(knowledge_description, "");
    }

    public Unlocks getUnlocks() {
        return unlocks;
    }

    @Override
    public String toString() {
        if (knowledge_description == null) {
            return "Knowledge: " + knowledge_name;
        }

        return "Knowledge: " + knowledge_name + ";"
                + "Description: " + knowledge_description;
    }
}
