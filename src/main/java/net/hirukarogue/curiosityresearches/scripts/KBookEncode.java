package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record KBookEncode (
        @Nullable String customName,
        List<String> associatedKnowledgeKeys,
        List<TreasureSubEncode> whereToFind
) {
    /*@Nullable
    private final String customName;
    private final List<KnowledgeSubEncode> associatedKnowledges;*/

    /*private KBookEncode(@Nullable String customName, List<KnowledgeSubEncode> associatedKnowledges) {
        this.customName = customName;
        this.associatedKnowledges = associatedKnowledges;
    }*/

    public static final Codec<KBookEncode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("custom_name").xmap(
                    optional -> optional.orElse(null),
                    customName -> customName != null ? Optional.of(customName) : Optional.empty()
            ).forGetter(n -> n.customName),
            Codec.list(Codec.STRING).fieldOf("associated_knowledge_keys").forGetter(KBookEncode::associatedKnowledgeKeys),
            Codec.list(TreasureSubEncode.CODEC).optionalFieldOf("where_to_find", List.of()).forGetter(KBookEncode::whereToFind)
    ).apply(instance, KBookEncode::new));

    /*public String getCustomName() {
        return customName;
    }
    public List<KnowledgeSubEncode> getAssociatedKnowledges() {
        return associatedKnowledges;
    }*/
}
