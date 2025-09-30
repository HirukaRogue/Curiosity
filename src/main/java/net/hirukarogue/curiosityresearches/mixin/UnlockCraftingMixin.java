package net.hirukarogue.curiosityresearches.mixin;

import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(CraftingMenu.class)
public class UnlockCraftingMixin extends RecipeBookMenu<CraftingContainer> {
    public UnlockCraftingMixin(MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @Inject(at = @At(value = "HEAD"),
            method = "slotChangedCraftingGrid", cancellable = true)
    private static void knowledgeCheck(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult, CallbackInfo ci) {
        KnowledgeData data = new KnowledgeData(pLevel);
        List<Knowledge> allKnowledge = data.loadCompleteEncyclopedia();
        List<Knowledge> playerKnowledge = data.loadPlayerKnowledge(pPlayer);

        for (Knowledge knowledge : allKnowledge) {
            if (knowledge.getUnlocks() != null && knowledge.getUnlocks().getUnlocked_recipes() != null) {
                Optional<CraftingRecipe> optional = Objects.requireNonNull(pLevel.getServer()).getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pContainer, pLevel);
                if (optional.isPresent()) {
                    CraftingRecipe recipe = optional.get();
                    if (knowledge.getUnlocks().getUnlocked_recipes().contains(recipe)) {
                        if (!playerKnowledge.contains(knowledge)) {
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {

    }

    @Override
    public void clearCraftingContent() {

    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
        return false;
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return 0;
    }

    @Override
    public int getGridHeight() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return null;
    }

    @Override
    public boolean shouldMoveToInventory(int pSlotIndex) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }
}
