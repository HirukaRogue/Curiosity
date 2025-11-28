package net.hirukarogue.curiosityresearches;

import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CuriosityCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CuriosityMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TUTORIALMOD_TAB =
            CREATIVE_MODE_TABS.register("curiosity_tab",() -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ResearchItemsRegistry.EPIC_RESEARCH.get()))
                    .title(Component.translatable("curiosity_researches.creative_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ResearchTableRegistry.RESEARCH_TABLE.get());

                        //research parchments
                        pOutput.accept(ResearchItemsRegistry.COMMON_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.COMMON_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.UNCOMMON_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.RARE_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.EPIC_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.LEGENDARY_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.MYTHIC_RESEARCH.get());

                        //books and notes
                        pOutput.accept(ResearchItemsRegistry.INCOMPLETE_RESEARCH.get());
                        pOutput.accept(ResearchItemsRegistry.RESEARCH_NOTES.get());
                        pOutput.accept(ResearchItemsRegistry.KNOWLEDGE_BOOK.get());
                        pOutput.accept(ResearchItemsRegistry.SHARED_RESEARCH_BOOK.get());

                        //ink and quill
                        pOutput.accept(ResearchItemsRegistry.INK_AND_QUILL.get());
                        pOutput.accept(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
