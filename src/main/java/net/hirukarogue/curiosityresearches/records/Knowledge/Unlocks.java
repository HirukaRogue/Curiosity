package net.hirukarogue.curiosityresearches.records.Knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public record Unlocks(
        String knowledgeName,
        int level,
        List<ResourceLocation> unlocks
) {
    public static final Codec<Unlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("knowledge_name").forGetter(Unlocks::knowledgeName),
            Codec.INT.fieldOf("level").forGetter(Unlocks::level),
            ResourceLocation.CODEC.listOf().optionalFieldOf("unlocks", List.of()).forGetter(Unlocks::unlocks)
    ).apply(instance, Unlocks::new));
}
