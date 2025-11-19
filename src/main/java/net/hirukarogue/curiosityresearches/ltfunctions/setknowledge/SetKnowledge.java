package net.hirukarogue.curiosityresearches.ltfunctions.setknowledge;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.KnowledgeBook;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class SetKnowledge extends LootItemConditionalFunction {
    final List<TagKey<Knowledge>> determinedKnowledge;

    protected SetKnowledge(LootItemCondition[] pPredicates, List<TagKey<Knowledge>> determinedKnowledge) {
        super(pPredicates);
        this.determinedKnowledge = determinedKnowledge;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        List<Knowledge> encyclopedia = new ArrayList<>();

        for (TagKey<Knowledge> kTag : determinedKnowledge) {
            Iterable<Holder<Knowledge>> iterableK = context.getLevel().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).getTagOrEmpty(kTag);
            iterableK.forEach(k -> encyclopedia.add(k.value()));
        }

        Item knowledgeBook = ResearchItemsRegistry.KNOWLEDGE_BOOK.get();


        ((KnowledgeBook) knowledgeBook).setBookKnowledges(encyclopedia);

        return new ItemStack(knowledgeBook);
    }

    @Override
    public LootItemFunctionType getType() {
        return null;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetKnowledge> {
        @Override
        public @NotNull SetKnowledge deserialize(JsonObject pObject, @NotNull JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
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

            return new SetKnowledge(pConditions, tags);
        }
    }
}
