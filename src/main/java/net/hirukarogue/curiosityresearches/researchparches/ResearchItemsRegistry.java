package net.hirukarogue.curiosityresearches.researchparches;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.InkandQuill;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.KnowledgeBook;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.ResearchParchment;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.SharedKnowledgeBook;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ResearchItemsRegistry {
    //items setup
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CuriosityMod.MOD_ID);

    //research parchments
    public static final RegistryObject<Item> COMMON_RESEARCH =
            ITEMS.register("common_research", () -> new ResearchParchment(new Item.Properties()));
    public static final RegistryObject<Item> UNCOMMON_RESEARCH =
            ITEMS.register("uncommon_research", () -> new ResearchParchment(new Item.Properties()));
    public static final RegistryObject<Item> RARE_RESEARCH =
            ITEMS.register("rare_research", () -> new ResearchParchment(new Item.Properties()));
    public static final RegistryObject<Item> EPIC_RESEARCH =
            ITEMS.register("epic_research", () -> new ResearchParchment(new Item.Properties()));
    public static final RegistryObject<Item> LEGENDARY_RESEARCH =
            ITEMS.register("legendary_research", () -> new ResearchParchment(new Item.Properties()));
    public static final RegistryObject<Item> MYTHIC_RESEARCH =
            ITEMS.register("mythic_research", () -> new ResearchParchment(new Item.Properties()));

    //research fragment
    public static final RegistryObject<Item> INCOMPLETE_RESEARCH =
            ITEMS.register("incomplete_research", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RESEARCH_NOTES =
            ITEMS.register("research_notes", () -> new Item(new Item.Properties()));

    //knowledge book
    public static final RegistryObject<Item> KNOWLEDGE_BOOK =
            ITEMS.register("knowledge_book", () -> new KnowledgeBook(new Item.Properties()));

    //to share research with friends
    public static final RegistryObject<Item> SHARED_RESEARCH_BOOK =
            ITEMS.register("shared_research_book", () -> new SharedKnowledgeBook(new Item.Properties()));

    //ink and quill to write research notes
    public static final RegistryObject<Item> INK_AND_QUILL =
            ITEMS.register("ink_and_quill", () -> new InkandQuill(new Item.Properties().durability(20)));
    public static final RegistryObject<Item> EMPTY_INK_AND_QUILL =
            ITEMS.register("empty_ink_and_quill", () -> new Item(new Item.Properties()));

    //item method setup
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
