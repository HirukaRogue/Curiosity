package net.hirukarogue.curiosityresearches.researchtable.researchtableblock;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.ResearchComponentContainer;
import net.hirukarogue.curiosityresearches.recipes.ResearchRecipes;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.ResearchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ResearchTableBlockEntity extends BlockEntity implements MenuProvider {
    private static final int PAPER_INPUT_SLOT = 0;
    private static final int PAPER_OUTPUT_SLOT = 1;
    private static final int INK_AND_QUILL_SLOT = 2;
    private static final int[] RESEARCH_ELEMENTS = {3,4,5,6,7,8,9};

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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory inventory, Player player) {
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

        CuriosityMod.LOGGER.debug("result: " + result);

        if (hasRecipe() && canInsertItemIntoOutputSlot()) {
            this.itemHandler.setStackInSlot(PAPER_OUTPUT_SLOT, new ItemStack(result.getItem(), 1));
        }
        isProcessing = false;
    }

    public void removeResult() {
        if (!hasRecipe() && !isProcessing) {
            isProcessing = true;
            CuriosityMod.LOGGER.debug("removing result");
            this.itemHandler.extractItem(PAPER_OUTPUT_SLOT, 1, false);
            isProcessing = false;
        }
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

    private void consumeInk() {
        if (this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT).isEmpty() || !this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT).is(ResearchItemsRegistry.INK_AND_QUILL.get())) {
            throw new RuntimeException("No ink and quill present");
        }

        this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT).hurt(1, this.level.random, null);
        if (this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT).getDamageValue() >= this.itemHandler.getStackInSlot(INK_AND_QUILL_SLOT).getMaxDamage()) {
            this.itemHandler.setStackInSlot(INK_AND_QUILL_SLOT, new ItemStack(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get()));
        }
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

    @Nullable
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

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack previous = lastStacks[slot];
            ItemStack current = getStackInSlot(slot);

            if (previous.isEmpty() && !current.isEmpty()) {
                if (!hasRecipe()) {
                    ResearchTableBlockEntity.this.removeResult();
                    lastStacks[slot] = current.copy();
                    return;
                }
                ResearchTableBlockEntity.this.popResult();
            } else if (!previous.isEmpty() && current.isEmpty()) {
                ResearchTableBlockEntity.this.removeResult();
            } else if (!ItemStack.isSameItemSameTags(previous, current)) {
                ResearchTableBlockEntity.this.popResult();
            } else {
                ResearchTableBlockEntity.this.removeResult();
                ResearchTableBlockEntity.this.popResult();
            }

            lastStacks[slot] = current.copy();
        }
    }
}
