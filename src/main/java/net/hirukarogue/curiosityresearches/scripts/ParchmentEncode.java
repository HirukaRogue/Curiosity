package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ParchmentEncode (
        @Nullable String customName,
        String tier,
        String knowledgeKey,
        @NotNull List<String> pattern,
        Map<String, Component> keys,
        int requiredPaper
        ) {
    /*@Nullable
    private final String customName;

    private final String tier;

    private final KnowledgeSubEncode knowledge;

    @NotNull
    private final List<String> pattern;
    private final Map<String, Component> keys;

    private final int requiredPaper;*/

    /*private ParchmentEncode(@Nullable String customName, String tier, KnowledgeSubEncode knowledge, List<String> pattern, Map<String, Component> keys, int requiredPaper) {
        this.customName = customName;
        this.tier = tier;
        this.knowledge = knowledge;
        this.pattern = pattern;
        this.keys = keys;
        this.requiredPaper = requiredPaper;
    }*/

    public static final Codec<ParchmentEncode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("custom_name", null).forGetter(ParchmentEncode::customName),
            Codec.STRING.flatXmap(s -> java.util.Set.of("common", "uncommon", "rare", "epic", "legendary", "MYTHIC").contains(s)
                            ? com.mojang.serialization.DataResult.success(s)
                            : com.mojang.serialization.DataResult.error(() -> "Invalid tier: " + s),
                    DataResult::success).fieldOf("tier").forGetter(ParchmentEncode::tier),
            Codec.STRING.fieldOf("knowledge_key").forGetter(ParchmentEncode::knowledgeKey),
            Codec.list(Codec.STRING).fieldOf("pattern").forGetter(ParchmentEncode::pattern),
            Codec.unboundedMap(Codec.STRING, Component.CODEC).fieldOf("keys").forGetter(ParchmentEncode::keys),
            Codec.INT.fieldOf("required_paper").forGetter(ParchmentEncode::requiredPaper)
    ).apply(instance, ParchmentEncode::new));

    /*public @Nullable String getCustomName() {
        return customName;
    }

    public String getTier() {
        return tier;
    }

    public KnowledgeSubEncode getKnowledge() {
        return knowledge;
    }

    public @NotNull List<String> getPattern() {
        return pattern;
    }

    public Map<String, Component> getKeys() {
        return keys;
    }

    public int getRequiredPaper() {
        return requiredPaper;
    }*/
}
