package net.hirukarogue.curiosityresearches.ltfunctions.setknowledge;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.KnowledgeBookData;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.KnowledgeBook;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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
import java.util.stream.StreamSupport;

public class SetKnowledge extends LootItemConditionalFunction
{
    final List<TagKey<Knowledge>> determinedKnowledge;
    final String customName;

    protected SetKnowledge(LootItemCondition[] pPredicates, List<TagKey<Knowledge>> determinedKnowledge, String customName)
    {
        super(pPredicates);
        this.determinedKnowledge = determinedKnowledge;
        this.customName = customName;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        List<Knowledge> encyclopedia = new ArrayList<>();

        Registry<Knowledge> knowledgeRegistry = context.getLevel().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY);

        for (TagKey<Knowledge> kTag : determinedKnowledge)
        {
            Iterable<Holder<Knowledge>> iterableK = knowledgeRegistry.getTagOrEmpty(kTag);
            iterableK.forEach(k -> encyclopedia.add(k.value()));
        }

        ItemStack knowledgeBook = new ItemStack(ResearchItemsRegistry.KNOWLEDGE_BOOK.get());

        ((KnowledgeBook) knowledgeBook.getItem()).setKnowledgeBookRecord(knowledgeBook, new KnowledgeBookData(encyclopedia, this.customName));
        return knowledgeBook;
    }

    @Override
    public LootItemFunctionType getType()
    {
        return null;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetKnowledge>
    {
        @Override
        public SetKnowledge deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions)
        {

            List<TagKey<Knowledge>> tags = new ArrayList<>();
            if (pObject.has("tags"))
            {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(pObject, "tags");

                List<String> tagsString = List.of(StreamSupport.stream(jsonArray.spliterator(), false)
                        .map(JsonElement::getAsString)
                        .toArray(String[]::new));

                if (tagsString.isEmpty())
                {
                    throw new RuntimeException("You have no registered knowledge tags for this loot table");
                }

                for (String tagString : tagsString)
                {
                    TagKey<Knowledge> wad = TagKey.create(CuriosityMod.KNOWLEDGE_REGISTRY, ResourceLocation.tryParse(tagString));
                    tags.add(wad);
                }
            }

            String customName = null;

            if (pObject.has("custom_name")) {
                customName = GsonHelper.getAsString(pObject, "custom_name");
            }

            return new SetKnowledge(pConditions, tags, customName);
        }
    }
}
