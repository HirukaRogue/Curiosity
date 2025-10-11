package net.hirukarogue.curiosityresearches.miscellaneous.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Unlocks {
    private final List<Item> unlocked_items;
    private final List<TagKey<Item>> unlocked_tags;
    private final List<Knowledge> unlocked_knowledge;
    private final List<Block> unlocked_blocks;
    private final List<String> unlocked_recipes;

    public static Codec<Unlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ForgeRegistries.ITEMS.getCodec())
                    .optionalFieldOf("unlocked_items", List.of()) // já define o default
                    .forGetter(unlocks -> unlocks.unlocked_items == null ? List.of() : unlocks.unlocked_items),
            Codec.list(TagKey.codec(ForgeRegistries.ITEMS.getRegistryKey()))
                    .optionalFieldOf("unlocked_tags", List.of())
                    .forGetter(unlocks -> unlocks.unlocked_tags == null ? List.of() : unlocks.unlocked_tags),
            Codec.list(Knowledge.CODEC)
                    .optionalFieldOf("unlocked_knowledge", List.of())
                    .forGetter(unlocks -> unlocks.unlocked_knowledge == null ? List.of() : unlocks.unlocked_knowledge),
            Codec.list(ForgeRegistries.BLOCKS.getCodec())
                    .optionalFieldOf("unlocked_blocks", List.of())
                    .forGetter(unlocks -> unlocks.unlocked_blocks == null ? List.of() : unlocks.unlocked_blocks),
            Codec.list(Codec.STRING)
                    .optionalFieldOf("unlocked_recipes", List.of())
                    .forGetter(unlocks -> unlocks.unlocked_recipes == null ? List.of() : unlocks.unlocked_recipes)
    ).apply(instance, Unlocks::new));

    public Unlocks(@Nullable List<Item> unlocked_items, @Nullable List<TagKey<Item>> unlocked_tags, @Nullable List<Knowledge> unlocked_knowledge, @Nullable List<Block> unlocked_blocks, @Nullable List<String> unlocked_recipes) {
        if (unlocked_items == null) {
            this.unlocked_items = new ArrayList<>();
        } else {
            this.unlocked_items = unlocked_items.isEmpty() ? new ArrayList<>() : unlocked_items;
        }

        if (unlocked_tags == null) {
            this.unlocked_tags = null;
        } else {
            this.unlocked_tags = unlocked_tags.isEmpty() ? new ArrayList<>() : unlocked_tags;
        }

        if (unlocked_knowledge == null) {
            this.unlocked_knowledge = new ArrayList<>();
        } else {
            this.unlocked_knowledge = unlocked_knowledge.isEmpty() ? new ArrayList<>() : unlocked_knowledge;
        }

        if (unlocked_blocks == null) {
            this.unlocked_blocks = new ArrayList<>();
        } else {
            this.unlocked_blocks = unlocked_blocks.isEmpty() ? new ArrayList<>() : unlocked_blocks;
        }

        if (unlocked_recipes == null) {
            this.unlocked_recipes = new ArrayList<>();
        } else {
            this.unlocked_recipes = unlocked_recipes.isEmpty() ? new ArrayList<>() : unlocked_recipes;
        }
    }

    public List<Item> getUnlocked_items() {
        return unlocked_items;
    }

    public List<TagKey<Item>> getUnlocked_tags() {
        return unlocked_tags;
    }

    public List<Knowledge> getUnlocked_knowledge() {
        return unlocked_knowledge;
    }

    public List<Block> getUnlocked_blocks() {
        return unlocked_blocks;
    }

    public List<String> getUnlocked_recipes() {
        return unlocked_recipes;
    }


}
