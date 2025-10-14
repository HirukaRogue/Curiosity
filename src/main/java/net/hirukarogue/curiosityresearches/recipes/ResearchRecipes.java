package net.hirukarogue.curiosityresearches.recipes;

import com.google.gson.JsonObject;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.ResearchComponentContainer;
import net.hirukarogue.curiosityresearches.miscellaneous.data.ResearchJsonCompiler;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.ResearchParchment;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class ResearchRecipes implements Recipe<ResearchComponentContainer> {
    private final NonNullList<Component> researchComponents;
    private final ItemStack paperInput;
    private final ItemStack finishedResearch;

    private final ResourceLocation id;

    public ResearchRecipes(NonNullList<Component> researchComponents, ItemStack paperInput, ItemStack finishedResearch, ResourceLocation id) {
        this.researchComponents = researchComponents;
        this.paperInput = paperInput;
        this.finishedResearch = finishedResearch;
        this.id = id;
    }


    @Override
    public boolean matches(ResearchComponentContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        if (!paperInput.is(Items.PAPER) || paperInput.getCount() > pContainer.getItem(0).getCount()) {
            return false;
        }

        if (pContainer.getItem(2).isEmpty()) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            if (researchComponents.get(i).getComponent().right().isPresent()) {
                List<Item> items = researchComponents.get(i).getComponent().map(
                        List::of,
                        tagKey -> StreamSupport.stream(pLevel.registryAccess().registryOrThrow(Registries.ITEM).getTagOrEmpty(tagKey).spliterator(), false).map(holder -> holder.value()).toList()
                );
                int finalI = i;
                if (items.stream().noneMatch(item -> ItemStack.isSameItem(item.getDefaultInstance(), pContainer.getItem(finalI+3)))) {
                    return false;
                }
            } else if (researchComponents.get(i).getComponent().left().isPresent()) {
                if (pContainer.getItem(i+3).isEmpty()) {
                    if (researchComponents.get(i).getComponent().left().get() == Items.AIR) {
                        continue;
                    }
                    return false;
                } else if (researchComponents.get(i).getComponent().left().get() == Items.AIR) {
                    CuriosityMod.LOGGER.debug("Returning false because air is required but something else is present");
                    return false;
                }

                Item item = researchComponents.get(i).getComponent().left().get();

                if (!ItemStack.isSameItem(new ItemStack(item), pContainer.getItem(i+3))) {
                    return false;
                }

            } else {
                return false;
            }

            if (researchComponents.get(i).getNeededQuantity() > pContainer.getItem(i+3).getCount()) {
                return false;
            }
        }

        return true;
    }

    public NonNullList<Component> getComponents() {
        return researchComponents;
    }

    @Override
    public ItemStack assemble(ResearchComponentContainer researchContainer, RegistryAccess registryAccess) {
        return finishedResearch.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return finishedResearch.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public int getPaperRequiredAmout() {
        return paperInput.getCount();
    }

    public int getComponentAmout(int componentIndex) {
        return researchComponents.get(componentIndex).getNeededQuantity();
    }

    public boolean isConsumed(int componentIndex) {
        return researchComponents.get(componentIndex).consumeComponent();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public String toString() {
        return "ResearchRecipes{" +
                "researchComponents=" + researchComponents +
                ", paperInput=" + paperInput +
                ", finishedResearch=" + finishedResearch +
                ", id=" + id +
                '}';
    }

    public static class Type implements RecipeType<ResearchRecipes> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "research";
    }

    public static class Serializer implements RecipeSerializer<ResearchRecipes> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(CuriosityMod.MOD_ID, "research");


        @Override
        public ResearchRecipes fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            NonNullList<Component> components_input = ResearchJsonCompiler.compile(pSerializedRecipe);

            CuriosityMod.LOGGER.debug("components_input: " + components_input);

            ItemStack output = new ItemStack(ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "research_result")));
            ItemStack paperRequired = new ItemStack(Items.PAPER, GsonHelper.getAsInt(pSerializedRecipe, "paper_required"));

            return new ResearchRecipes(components_input, paperRequired, output, pRecipeId);
        }

        @Override
        public @Nullable ResearchRecipes fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Component> inputs = NonNullList.withSize(pBuffer.readInt(), new Component(ItemStack.EMPTY));
            ItemStack paper = pBuffer.readItem();

            inputs.replaceAll(ignored -> new Component(pBuffer.readItem(), pBuffer.readBoolean()));

            ItemStack output = pBuffer.readItem();
            return new ResearchRecipes(inputs, paper, output, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ResearchRecipes pRecipe) {
            pBuffer.writeInt(pRecipe.researchComponents.size());
            pBuffer.writeItemStack(pRecipe.paperInput, false);

            for (Component stack : pRecipe.researchComponents) {
                if (stack.getComponent().left().isPresent()){
                    pBuffer.writeItemStack(new ItemStack(stack.getComponent().left().get(), stack.getNeededQuantity()),false);
                } else if (stack.getComponent().right().isPresent()) {
                    List<ItemStack> itemStacks = new ArrayList<>();
                    List<Item> items = new ArrayList<>();
                    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(stack.getComponent().right().get())) {
                        items.add(holder.value());
                    }

                    for (Item item : items) {
                        itemStacks.add(new ItemStack(item, stack.getNeededQuantity()));
                    }

                    pBuffer.writeCollection(itemStacks, (buf, stackItem) -> buf.writeItemStack(stackItem, false));
                }
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
        }
    }
}

