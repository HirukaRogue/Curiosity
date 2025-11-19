package net.hirukarogue.curiosityresearches.mixin;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(CraftingMenu.class)
public abstract class UnlockCraftingMixin extends RecipeBookMenu<CraftingContainer> {
    public UnlockCraftingMixin(MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @Inject(at = @At(value = "HEAD"),
            method = "slotChangedCraftingGrid", cancellable = true)
    private static void knowledgeCheck(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult, CallbackInfo ci) {
        List<Knowledge> playerKnowledge = KnowledgeHelper.getPlayerKnowledge(pPlayer);

        if (pLevel.isClientSide()) {
            return;
        }
        Optional<CraftingRecipe> optional = Objects.requireNonNull(pLevel.getServer()).getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pContainer, pLevel);
        if (optional.isPresent()) {
            CraftingRecipe recipe = optional.get();
            if (!curiosity$canCraft(pLevel, playerKnowledge, recipe)) {
                pResult.setItem(0, ItemStack.EMPTY);
                ci.cancel();
            }
        }
    }

    @Unique
    private static boolean curiosity$canCraft(Level level, List<Knowledge> playerKnowledge, CraftingRecipe recipe) {
        for (net.minecraft.world.item.crafting.Ingredient ingredient : recipe.getIngredients()) {
            // Checa se a própria `Ingredient` (por exemplo uma tag) tem um unlock associado.
            Unlocks ingredientUnlocks = KnowledgeHelper.getUnlockFromIngredient(level, ingredient);
            Knowledge requiredIngredientKnowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, ingredientUnlocks);
            if (requiredIngredientKnowledge != null && !playerKnowledge.contains(requiredIngredientKnowledge)) {
                return false;
            }

            // Se não bloqueado pela tag, checa os itens expandidos normalmente.
            for (ItemStack itemStack : ingredient.getItems()) {
                Unlocks relatedUnlocks = KnowledgeHelper.getUnlockFromItem(level, itemStack);
                Knowledge required = KnowledgeHelper.getKnowledgeFromUnlock(level, relatedUnlocks);
                if (required != null && !playerKnowledge.contains(required)) {
                    return false;
                }
            }
        }

        // Checa o resultado da receita
        ItemStack result = recipe.getResultItem(level.registryAccess());
        Unlocks resultUnlocks = KnowledgeHelper.getUnlockFromItem(level, result);
        Knowledge requiredResult = KnowledgeHelper.getKnowledgeFromUnlock(level, resultUnlocks);
        if (requiredResult != null && !playerKnowledge.contains(requiredResult)) {
            return false;
        }

        return true;
    }
}
