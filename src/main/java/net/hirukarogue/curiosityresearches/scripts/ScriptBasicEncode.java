package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptBasicEncode {
    @Nullable
    private final String customName;

    private final String knowledgeName;
    private final String knowledgeDescription;
    private final String knowledgeTier;
    private final List<ScriptElement> thingsToUnlock;

    @NotNull
    private final List<String> pattern;
    private final Map<String, Component> keys;

    private final int requiredPaper;

    private ScriptBasicEncode(@Nullable String customName, String knowledgeName, String knowledgeDescription, String knowledgeTier, List<ScriptElement> thingsToUnlock, List<String> pattern, Map<String, Component> keys, int requiredPaper) {
        this.customName = customName;
        this.knowledgeName = knowledgeName;
        this.knowledgeDescription = knowledgeDescription;
        this.knowledgeTier = knowledgeTier;
        this.thingsToUnlock = thingsToUnlock;
        this.pattern = pattern;
        this.keys = keys;
        this.requiredPaper = requiredPaper;
    }

    public static final Codec<ScriptBasicEncode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("custom_name", null).forGetter(ScriptBasicEncode::getCustomName),
            Codec.STRING.fieldOf("knowledge_name").forGetter(ScriptBasicEncode::getKnowledgeName),
            Codec.STRING.optionalFieldOf("knowledge_description", "").forGetter(ScriptBasicEncode::getKnowledgeDescription),
            Codec.STRING.flatXmap(s -> java.util.Set.of("common", "uncommon", "rare", "epic", "legendary", "MYTHIC").contains(s)
                            ? com.mojang.serialization.DataResult.success(s)
                            : com.mojang.serialization.DataResult.error(() -> "Invalid knowledge_tier: " + s),
                    DataResult::success).fieldOf("knowledge_tier").forGetter(ScriptBasicEncode::getKnowledgeTier),
            Codec.list(ScriptElement.CODEC).optionalFieldOf("things_to_unlock", List.of()).forGetter(ScriptBasicEncode::getThingsToUnlock),
            Codec.list(Codec.STRING).fieldOf("pattern").forGetter(ScriptBasicEncode::getPattern),
            Codec.unboundedMap(Codec.STRING, Component.CODEC).fieldOf("keys").forGetter(ScriptBasicEncode::getKeys),
            Codec.INT.fieldOf("required_paper").forGetter(ScriptBasicEncode::getRequiredPaper)
    ).apply(instance, ScriptBasicEncode::new));

    public String getCustomName() {
        return customName;
    }

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

    public @NotNull List<String> getPattern() {
        return pattern;
    }

    public Map<String, Component> getKeys() {
        return keys;
    }

    public int getRequiredPaper() {
        return requiredPaper;
    }
}
