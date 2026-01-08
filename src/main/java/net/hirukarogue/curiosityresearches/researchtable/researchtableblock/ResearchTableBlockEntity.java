package net.hirukarogue.curiosityresearches.researchtable.researchtableblock;

import com.mojang.datafixers.util.Either;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.ResearchComponentContainer;
import net.hirukarogue.curiosityresearches.recipes.ResearchRecipes;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.ResearchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ResearchTableBlockEntity extends BlockEntity implements MenuProvider {
    private static final int PAPER_INPUT_SLOT = 0;
    private static final int PAPER_OUTPUT_SLOT = 1;
    private static final int INK_AND_QUILL_SLOT = 2;
    private static final int[] RESEARCH_ELEMENTS = {3,4,5,6,7,8,9};

    private Player player;

    private final ResearchStackHandler itemHandler = new ResearchStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                if (slot != PAPER_OUTPUT_SLOT) {
                    super.onContentsChanged(slot);
                }
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHander = LazyOptional.empty();

    public ResearchTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ResearchTableEntity.RESEARCH_TABLE_BE.get(), pPos, pBlockState);
    }

    public ItemStack getRenderStack() {
        if (itemHandler.getStackInSlot(PAPER_OUTPUT_SLOT).isEmpty()) {
            return itemHandler.getStackInSlot(PAPER_INPUT_SLOT);
        } else {
            return itemHandler.getStackInSlot(PAPER_OUTPUT_SLOT);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHander.cast();
        }

        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHander = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHander.invalidate();
    }

    public void drops() {
        ResearchComponentContainer inventory = new ResearchComponentContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.curiosiry.research_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory inventory, Player player) {
        this.player = player;
        return new ResearchMenu(pContainerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    private boolean isProcessing = false;

    public void popResult() {
        if (isProcessing) return;
        isProcessing = true;

        Optional<ResearchRecipes> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            isProcessing = false;
            return;
        }

        ItemStack result = recipe.get().getResultItem(null);

        List<Knowledge> playerKnowledges = KnowledgeHelper.getPlayerKnowledge(this.player);
        if (level == null) {
            isProcessing = false;
            return;
        }
        List<Knowledge> allKnowledge = level.registryAccess().registry(CuriosityMod.KNOWLEDGE_REGISTRY).get().stream().toList();
        for (Knowledge knowledge : allKnowledge) {
            Unlocks unlocks = KnowledgeHelper.getKnowledgeUnlock(level, knowledge);
            if (unlocks == null) {
                continue;
            }
            for (ResourceLocation krl : unlocks.unlocks()) {
                Knowledge unlock_knowledge = level.registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).get(krl);
                if (unlock_knowledge == null) {
                    continue;
                }
                if (playerKnowledges.contains(unlock_knowledge)) {
                    isProcessing = false;
                    CuriosityMod.LOGGER.debug("player does not have knowledge: " + knowledge.knowledgeName());
                    return;
                }
            }
        }

        CuriosityMod.LOGGER.debug("result: " + result);

        if (hasRecipe() && canInsertItemIntoOutputSlot()) {
            this.itemHandler.setStackInSlot(PAPER_OUTPUT_SLOT, result);
        }
        isProcessing = false;
    }

    public void consumeForResearch() {
        Optional<ResearchRecipes> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            return;
        }

        for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
            if (recipe.get().isConsumed(i)) {
                this.itemHandler.extractItem(RESEARCH_ELEMENTS[i], recipe.get().getComponentAmout(i), false);
            }
        }
        this.itemHandler.extractItem(PAPER_INPUT_SLOT, recipe.get().getPaperRequiredAmout(), false);
        consumeInk();
    }

    public String research() {
        if (level == null || level.isClientSide()) {
            return "";
        }

        if (hasRecipe()) {
            consumeForResearch();
            popResult();
            return "Research complete!";
        }

        return "Not enough components to complete the research.";
    }

    public String ponder() {
        if (level == null || level.isClientSide()) {
            return "";
        }

        boolean hasItem = false;
        for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
            if (!this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]).isEmpty()) {
                hasItem = true;
                break;
            }
        }

        if (!hasItem) {
            return "There is nothing to ponder upon.";
        }

        if (this.itemHandler.getStackInSlot(PAPER_INPUT_SLOT).isEmpty() || !this.itemHandler.getStackInSlot(PAPER_INPUT_SLOT).is(Items.PAPER)) {
            return "Not enough paper.";
        }

        int[] presentCounts = new int[RESEARCH_ELEMENTS.length];
        for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
            ItemStack s = this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]);
            presentCounts[i] = s.isEmpty() ? 0 : s.getCount();
        }

        List<ResearchRecipes> matches = getMatchingRecipes();

        if (matches.isEmpty()) {
            return "You won't learn anything new by pondering upon these components.";
        }

        int randomRecipe = this.level.random.nextInt(matches.size());
        ResearchRecipes selected = matches.get(randomRecipe);

        List<String> possibleParts = getAllPossibleParts(selected);

        if (possibleParts.isEmpty()) {
            return "guess it's everything in order.";
        }

        int select = this.level.random.nextInt(possibleParts.size());
        String whichPart = possibleParts.get(select);

        return pickMessage(whichPart, selected);
    }

    private String pickMessage(String choose, ResearchRecipes selected) {
        String message = "";
        switch (choose) {
            case "misspositioned component" -> {
                List<ItemStack> misspositionedItem = getAllMisspositionedItem(selected);
                int randomIndex = this.level.random.nextInt(misspositionedItem.size());
                ItemStack item = misspositionedItem.get(randomIndex);
                for (int i = 0; i < selected.getComponents().size(); i++) {
                    if (selected.getComponents().get(i).getComponent().left().isPresent()) {
                        if (selected.getComponents().get(i).getComponent().left().isEmpty()) {
                            continue;
                        }
                        if (item.getItem() == selected.getComponents().get(i).getComponent().left().get()) {
                            Item itemObj = selected.getComponents().get(i).getComponent().left().get();
                            String itemName = (new ItemStack(itemObj)).getHoverName().getString();

                            if (!consumeForPonder()) {
                                message = "not enough ink.";
                                break;
                            }

                            switch (i) {
                                case 0 -> {
                                    message = "maybe i should place " + itemName + " in the top left slot.";
                                }
                                case 1 -> {
                                    message = "maybe i should place " + itemName + " in the top right slot.";
                                }
                                case 2 -> {
                                    message = "maybe i should place " + itemName + " in the middle left slot.";
                                }
                                case 3 -> {
                                    message = "maybe i should place " + itemName + " in the central slot.";
                                }
                                case 4 -> {
                                    message = "maybe i should place " + itemName + " in the middle right slot.";
                                }
                                case 5 -> {
                                    message = "maybe i should place " + itemName + " in the bottom left slot.";
                                }
                                case 6 -> {
                                    message = "maybe i should place " + itemName + " in the bottom right slot.";
                                }
                                default -> {
                                    message = "guess it's everything in order";
                                }
                            }
                        }
                    }

                    if (selected.getComponents().get(i).getComponent().right().isPresent()) {
                        AtomicBoolean isTag = new AtomicBoolean(false);
                        int copy = i;
                        selected.getComponents().get(i).getComponent().right().stream().forEach(tag -> {
                            if (item.getItem() == itemHandler.getStackInSlot(copy).getItem()) {
                                isTag.set(true);
                            }
                        });
                        if (isTag.get()) {
                            String itemName = itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]).getHoverName().getString();

                            if (!consumeForPonder()) {
                                message = "not enough ink.";
                            }

                            switch (i) {
                                case 0 -> {
                                    message = "maybe i should place " + itemName + " in the top left slot.";
                                }
                                case 1 -> {
                                    message = "maybe i should place " + itemName + " in the top right slot.";
                                }
                                case 2 -> {
                                    message = "maybe i should place " + itemName + " in the middle left slot.";
                                }
                                case 3 -> {
                                    message = "maybe i should place " + itemName + " in the central slot.";
                                    break;
                                }
                                case 4 -> {
                                    message = "maybe i should place " + itemName + " in the middle right slot.";
                                }
                                case 5 -> {
                                    message = "maybe i should place " + itemName + " in the bottom left slot.";
                                }
                                case 6 -> {
                                    message = "maybe i should place " + itemName + " in the bottom right slot.";
                                }
                                default -> {
                                    message = "guess it's everything in order";
                                }
                            }
                        }
                    }
                }
            }
            case "insufficient quantity" -> {
                List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> insufComponents = getAllInsuficientComponents(selected);
                int randomIndex = this.level.random.nextInt(insufComponents.size());
                net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component comp = insufComponents.get(randomIndex);

                if (comp.getComponent().left().isPresent()) {
                    ItemStack itemStack = new ItemStack(comp.getComponent().left().get());
                    String itemName = itemStack.getHoverName().getString();
                    if (!consumeForPonder()) {
                        message = "not enough ink.";
                        break;
                    }

                    message = "maybe i should try adding more " + itemName + ".";
                } else if (comp.getComponent().right().isPresent()) {
                    AtomicReference<List<ItemStack>> itemStack = new AtomicReference<>(new ArrayList<>());
                    AtomicBoolean isTag = new AtomicBoolean(false);
                    comp.getComponent().right().stream().forEach(tag -> {
                        isTag.set(true);
                        List<Item> itemsInTag = level.holderLookup(ForgeRegistries.ITEMS.getRegistryKey())
                                .get(tag)
                                .map(HolderSet::stream)
                                .orElse(Stream.empty())
                                .map(Holder::value)
                                .toList();
                        if (!itemsInTag.isEmpty()) {
                            itemStack.set(new ArrayList<>());
                            for (Item item : itemsInTag) {
                                itemStack.get().add(new ItemStack(item));
                            }
                        }
                    });
                    if (isTag.get()) {
                        StringBuilder itemsName = new StringBuilder();
                        for (int j = 0; j < itemStack.get().size(); j++) {
                            String itemName = itemStack.get().get(j).getHoverName().getString();
                            itemsName.append(itemName);
                            if (j < itemStack.get().size() - 2) {
                                itemsName.append(", ");
                            } else if (j == itemStack.get().size() - 2) {
                                itemsName.append(" or ");
                            }
                        }
                        if (!consumeForPonder()) {
                            message = "not enough ink.";
                            break;
                        }

                        message = "maybe i should try adding more " + itemsName + ".";
                    } else {
                        message = "guess it's everything in order.";
                    }
                }

            }
            case "lacking components" -> {
                List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> requiredComponents = getAllRequiredComponents(selected);
                Either<Item, TagKey<Item>> selectedItem = null;
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
                    ItemStack s = this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]);
                    stacks.add(s);
                }

                int index = this.level.random.nextInt(requiredComponents.size());
                selectedItem = requiredComponents.get(index).getComponent();

                if (!consumeForPonder()) {
                    message = "not enough ink.";
                    break;
                }

                if (selectedItem.left().isPresent()) {
                    Item itemObj = selectedItem.left().get();
                    String itemName = (new ItemStack(itemObj)).getHoverName().getString();
                    message = "maybe i should try adding " + itemName + ".";
                    break;
                } else if (selectedItem.right().isPresent()) {
                    StringBuilder items = new StringBuilder();
                    List<String> itemNames = new ArrayList<>();
                    selectedItem.right().stream().forEach(tag -> {
                        Collection<Item> itemsInTag = level.holderLookup(ForgeRegistries.ITEMS.getRegistryKey())
                                .get(tag)
                                .map(HolderSet::stream)
                                .orElse(Stream.empty())
                                .map(Holder::value)
                                .toList();
                        for (Item item : itemsInTag) {
                            String itemName = (new ItemStack(item)).getHoverName().getString();
                            itemNames.add(itemName);
                        }
                    });
                    for (int i = 0; i < itemNames.size(); i++) {
                        items.append(itemNames.get(i));
                        if (i < itemNames.size() - 2) {
                            items.append(", ");
                        } else if (i == itemNames.size() - 2) {
                            items.append(" or ");
                        }
                    }

                    message = "maybe i should try adding " + items + ".";
                    break;
                }

                message = "guess it's everything in order.";
            }
            default -> {
                message = "guess it's everything in order.";
            }
        }

        return message;
    }

    private List<ItemStack> getAllMisspositionedItem(ResearchRecipes selected){
        List<ItemStack> misspositionedItem = new ArrayList<>();
        for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
            ItemStack s = this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]);
            if (!s.isEmpty()) {
                boolean correctPosition = false;
                if (selected.getComponents().get(i).getComponent().left().isPresent()) {
                    if (selected.getComponents().get(i).getComponent().left().get() == s.getItem()) {
                        correctPosition = true;
                    }
                } else if (selected.getComponents().get(i).getComponent().right().isPresent()) {
                    AtomicBoolean isTag = new AtomicBoolean(false);
                    int copy = i;
                    selected.getComponents().get(i).getComponent().right().stream().forEach(tag -> {
                        if (s.is(tag)) {
                            isTag.set(true);
                        }
                    });
                    if (isTag.get()) {
                        correctPosition = true;
                    }
                }
                if (!correctPosition) {
                    misspositionedItem.add(s);
                }
            }
        }

        return misspositionedItem;
    }

    private List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> getAllInsuficientComponents(ResearchRecipes selected) {
        List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> insufComponents = new ArrayList<>();
        for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
            if (this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]).isEmpty()) {
                continue;
            }
            for (net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component comp : selected.getComponents()) {
                if (comp.getComponent().left().isPresent()) {
                    ItemStack s = this.itemHandler.getStackInSlot(RESEARCH_ELEMENTS[i]);
                    if (comp.getComponent().left().get().equals(s.getItem())) {
                        if (s.getCount() < selected.getComponentAmout(i)) {
                            insufComponents.add(comp);
                            break;
                        }
                    }
                } else if (comp.getComponent().right().isPresent()) {
                    AtomicBoolean insuficientForTag = new AtomicBoolean(false);
                    final int currentSlot = RESEARCH_ELEMENTS[i];
                    int index = i;
                    comp.getComponent().right().stream().forEach(tag -> {
                        if (itemHandler.getStackInSlot(currentSlot).is(tag)) {
                            ItemStack s = itemHandler.getStackInSlot(currentSlot);
                            if (s.getCount() < selected.getComponentAmout(index)) {
                                insuficientForTag.set(true);
                            }
                        }
                    });
                    if (insuficientForTag.get()) {
                        insufComponents.add(comp);
                        break;
                    }
                }
            }
        }
        return insufComponents;
    }

    private List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> getAllRequiredComponents(ResearchRecipes selected) {
        List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> requiredComponents = new ArrayList<>();
        for (net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component comp : selected.getComponents()) {
            if (comp.getComponent().left().isPresent()) {
                if (comp.getComponent().left().get().equals(Items.AIR)) {
                    continue;
                }
                requiredComponents.add(comp);
            } else if (comp.getComponent().right().isPresent()) {
                requiredComponents.add(comp);
            }
        }
        for (int researchElement : RESEARCH_ELEMENTS) {
            ItemStack s = this.itemHandler.getStackInSlot(researchElement);
            if (s.isEmpty()) {
                continue;
            }

            requiredComponents.removeIf(comp -> {
                if (comp.getComponent().left().isPresent()) {
                    return comp.getComponent().left().get() == s.getItem();
                } else if (comp.getComponent().right().isPresent()) {
                    AtomicBoolean isTag = new AtomicBoolean(false);
                    comp.getComponent().right().stream().forEach(tag -> {
                        if (s.is(tag)) {
                            isTag.set(true);
                        }
                    });
                    return isTag.get();
                }
                return false;
            });
        }
        return requiredComponents;
    }

    private List<String> getAllPossibleParts(ResearchRecipes selected) {
        List<ItemStack> misspositionedItem = getAllMisspositionedItem(selected);

        boolean insuficientQuantity = false;
        List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> insufComponents = getAllInsuficientComponents(selected);

        if (!insufComponents.isEmpty()) {
            insuficientQuantity = true;
        }

        boolean lackingcomponent = false;
        List<net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component> requiredComponents = getAllRequiredComponents(selected);
        if (!requiredComponents.isEmpty()) {
            lackingcomponent = true;
        }

        List<String> possibleParts = new ArrayList<>();
        if (!misspositionedItem.isEmpty()) {
            possibleParts.add("misspositioned component");
        }
        if (insuficientQuantity) {
            possibleParts.add("insufficient quantity");
        }
        if (lackingcomponent) {
            possibleParts.add("lacking components");
        }

        return possibleParts;
    }

    private List<ResearchRecipes> getMatchingRecipes() {
        List<ResearchRecipes> allRecipes = this.level.getRecipeManager().getAllRecipesFor(ResearchRecipes.Type.INSTANCE);
        List<ResearchRecipes> matches = new java.util.ArrayList<>();

        for (ResearchRecipes r : allRecipes) {
            boolean ok = false;
            List<Item> requiredItems = new ArrayList<>();

            for (net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component c : r.getComponents()) {
                if (c.getComponent().left().isPresent()) {
                    if (c.getComponent().left().get().equals(Items.AIR)) {
                        continue;
                    }
                    requiredItems.add(c.getComponent().left().get());
                }
                if (c.getComponent().right().isPresent()) {
                    c.getComponent().right().ifPresent(tag -> {
                        List<Item> itemsInTag = level.holderLookup(ForgeRegistries.ITEMS.getRegistryKey())
                                .get(tag)
                                .map(HolderSet::stream)
                                .orElse(Stream.empty())
                                .map(Holder::value)
                                .toList();
                        requiredItems.addAll(itemsInTag);
                    });
                }
            }

            for (int i = 0; i < RESEARCH_ELEMENTS.length; i++) {
                if (itemHandler.getStackInSlot(i).isEmpty()) {
                    continue;
                }

                ok = requiredItems.contains(itemHandler.getStackInSlot(i).getItem());
            }
            if (ok) matches.add(r);
        }

        return matches;
    }

    private boolean consumeForPonder() {
        if (!consumeInk()) {
            return false;
        }

        // Consumir o Papel corretamente
        ItemStack paperStack = this.itemHandler.getStackInSlot(PAPER_INPUT_SLOT).copy();
        paperStack.shrink(1);
        this.itemHandler.setStackInSlot(PAPER_INPUT_SLOT, paperStack);

        // Consumir os componentes de pesquisa
        for (int i : RESEARCH_ELEMENTS) {
            ItemStack s = itemHandler.getStackInSlot(i);
            if (s.isEmpty() || s.getItem() instanceof BucketItem) continue;

            ItemStack componentCopy = s.copy();
            componentCopy.shrink(1);
            itemHandler.setStackInSlot(i, componentCopy);
        }

        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    private boolean consumeInk() {
        ItemStack inkStack = this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT);

        if (inkStack.isEmpty() || !inkStack.is(ResearchItemsRegistry.INK_AND_QUILL.get())) {
            return false;
        }

        // 1. Criamos uma cópia para manipular com segurança
        ItemStack copy = inkStack.copy();
        int newDamage = copy.getDamageValue() + 1;

        if (newDamage >= copy.getMaxDamage()) {
            // Substitui pelo frasco vazio
            this.itemHandler.setStackInSlot(INK_AND_QUILL_SLOT, new ItemStack(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get()));
        } else {
            // Aplica o dano e REINSERE no slot para forçar a atualização
            copy.setDamageValue(newDamage);
            this.itemHandler.setStackInSlot(INK_AND_QUILL_SLOT, copy);
        }

        // Força o bloco a salvar os dados
        this.setChanged();
        return true;
    }

    private boolean hasRecipe() {
        Optional<ResearchRecipes> recipe = getCurrentRecipe();

        return recipe.isPresent();
    }

    private Optional<ResearchRecipes> getCurrentRecipe() {
        ResearchComponentContainer inventory = new ResearchComponentContainer(this.itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        CuriosityMod.LOGGER.debug("recipe: " + this.level.getRecipeManager().getRecipeFor(ResearchRecipes.Type.INSTANCE, inventory, level));

        return this.level.getRecipeManager().getRecipeFor(ResearchRecipes.Type.INSTANCE, inventory, level);
    }

    private boolean canInsertItemIntoOutputSlot() {
        return this.itemHandler.getStackInSlot(PAPER_OUTPUT_SLOT).isEmpty();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public class ResearchStackHandler extends ItemStackHandler {
        private final ItemStack[] lastStacks;

        public ResearchStackHandler(int slots) {
            super(slots);
            lastStacks = new ItemStack[slots];
            for (int i = 0; i < slots; i++) {
                lastStacks[i] = ItemStack.EMPTY;
            }
        }
    }
}
