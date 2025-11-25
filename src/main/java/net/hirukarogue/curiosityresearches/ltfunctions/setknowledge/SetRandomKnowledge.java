package net.hirukarogue.curiosityresearches.ltfunctions.setknowledge;

import com.google.gson.*;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.KnowledgeBookData;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.KnowledgeBook;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class SetRandomKnowledge extends LootItemConditionalFunction {
    final List<TagKey<Knowledge>> determinedKnowledge;
    final String customName;
    final int max_amout;
    final int min_amout;

    protected SetRandomKnowledge(LootItemCondition[] pPredicates, List<TagKey<Knowledge>> determinedKnowledge, int max_amout, int min_amout, String customName) {
        super(pPredicates);
        this.determinedKnowledge = determinedKnowledge;
        this.customName = customName;
        this.max_amout = max_amout;
        this.min_amout = min_amout;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        List<Knowledge> possible = new ArrayList<>();

        for (TagKey<Knowledge> kTag : determinedKnowledge) {
            Iterable<Holder<Knowledge>> iterableK = context.getLevel().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).getTagOrEmpty(kTag);
            iterableK.forEach(k -> possible.add(k.value()));
        }

        int knowledges_amout = context.getRandom().nextIntBetweenInclusive(min_amout, max_amout);

        List<Knowledge> selected = new ArrayList<>();

        for (int i = 0; i < knowledges_amout; i++) {
            int index;
            do {
                index = context.getRandom().nextIntBetweenInclusive(0, possible.size());
            } while (selected.contains(possible.get(index)));

            Knowledge knowledge = possible.get(index);

            selected.add(knowledge);
        }

        ItemStack knowledgeBook = new ItemStack(ResearchItemsRegistry.KNOWLEDGE_BOOK.get());

        KnowledgeBook.setKnowledgeBookRecord(knowledgeBook, new KnowledgeBookData(selected, this.customName));

        return knowledgeBook;
    }

    @Override
    public LootItemFunctionType getType() {
        return null;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetRandomKnowledge> {
        @Override
        public SetRandomKnowledge deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            List<TagKey<Knowledge>> tags = new ArrayList<>();
            if (pObject.has("tags")) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(pObject, "tags");

                List<String> tagsString = List.of(StreamSupport.stream(jsonArray.spliterator(), false)
                        .map(JsonElement::getAsString)
                        .toArray(String[]::new));

                if (tagsString.isEmpty()) {
                    throw new RuntimeException("You have no registered knowledge tags for this loot table");
                }

                for (String tagString : tagsString) {
                    TagKey<Knowledge> tag = null;
                    TagKey.create(CuriosityMod.KNOWLEDGE_REGISTRY, Objects.requireNonNull(ResourceLocation.tryParse(tagString)));
                    tags.add(tag);
                }
            }

            int max;
            int min;

            if(pObject.has("max_knowledges")) {
                max = GsonHelper.getAsInt(pObject, "max_knowledges");
                if (max > tags.size()) {
                    throw new IllegalArgumentException("The max amout of knowledge overpasses the amout of available knowledge");
                }
            } else {
                max = tags.size();
            }

            if(pObject.has("min_knowledges")) {
                min = GsonHelper.getAsInt(pObject, "min_knowledges");
                if (min > max) {
                    throw new IllegalArgumentException("The min amout of knowledge should never be higher than the max amout of available knowledges");
                }
            } else {
                min = 0;
            }

            String customName = null;

            if (pObject.has("custom_name")) {
                customName = GsonHelper.getAsString(pObject, "custom_name");
            }

            return new SetRandomKnowledge(pConditions, tags, max, min, customName);
        }
    }
}
