package net.hirukarogue.curiosityresearches.recipes;

import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ResearchCraft extends RecipeProvider {

    public ResearchCraft(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResearchTableRegistry.RESEARCH_TABLE.get())
                .pattern(" e ")
                .pattern("ppp")
                .pattern("f f")
                .define('p', ItemTags.PLANKS)
                .define('e', Items.PAPER)
                .define('f', ItemTags.FENCES)
                .unlockedBy(getHasName(Items.WRITABLE_BOOK), has(Items.WRITABLE_BOOK))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ResearchItemsRegistry.INK_AND_QUILL.get(), 1)
                .requires(Items.BLACK_DYE)
                .requires(Items.FEATHER)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy(getHasName(Items.BLACK_DYE), has(Items.BLACK_DYE))
                .unlockedBy(getHasName(Items.INK_SAC), has(Items.INK_SAC))
                .save(pWriter, new ResourceLocation("curiosity_researches","ink_and_quill_from_scratch"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ResearchItemsRegistry.SHARED_RESEARCH_BOOK.get(), 1)
                .requires(Items.WRITABLE_BOOK)
                .requires(Items.DIAMOND)
                .unlockedBy(getHasName(Items.WRITABLE_BOOK), has(Items.WRITABLE_BOOK))
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(pWriter, new ResourceLocation("curiosity_researches","shared_research_book"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ResearchItemsRegistry.INK_AND_QUILL.get(), 1)
                .requires(Items.BLACK_DYE)
                .requires(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get())
                .unlockedBy(getHasName(Items.BLACK_DYE), has(Items.BLACK_DYE))
                .unlockedBy(getHasName(Items.INK_SAC), has(Items.INK_SAC))
                .save(pWriter, new ResourceLocation("curiosity_researches","ink_and_quill_refill"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get(), 1)
                .requires(Items.FEATHER)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER))
                .save(pWriter, new ResourceLocation("curiosity_researches","bottle_and_quill"));
    }
}