package net.hirukarogue.curiosityresearches.mixin;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(SmithingMenu.class)
public abstract class UnlockSmithingMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private List<SmithingRecipe> recipes;
    @Shadow
    private SmithingRecipe selectedRecipe;
    @Shadow
    @Final
    private Level level;


    public UnlockSmithingMixin(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pType, pContainerId, pPlayerInventory, pAccess);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        Player player = this.player;

        List<Knowledge> playerKnowledge = KnowledgeHelper.getPlayerKnowledge(player);

        curiosity$canCraft(this.level, playerKnowledge);
    }

    @Unique
    private void curiosity$canCraft(Level level, List<Knowledge> playerKnowledge) {
        for (Unlocks unlocks : level.registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY)) {
            for (ResourceLocation rl : unlocks.unlocks()) {
                Optional<SmithingRecipe> optRecipe = Objects.requireNonNull(level.getServer())
                        .getRecipeManager().byKey(rl)
                        .filter(r -> r instanceof SmithingRecipe)
                        .map(r -> (SmithingRecipe) r);

                if (optRecipe.isPresent()) {
                    SmithingRecipe recipe = optRecipe.get();
                    boolean missingKnowledge = false;
                    for (net.minecraft.world.item.crafting.Ingredient ingredient : recipe.getIngredients()) {
                        // Checa se a própria `Ingredient` (por exemplo uma tag) tem um unlock associado.
                        Unlocks ingredientUnlocks = KnowledgeHelper.getUnlockFromIngredient(level, ingredient);
                        Knowledge requiredIngredientKnowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, ingredientUnlocks);
                        if (requiredIngredientKnowledge != null && !playerKnowledge.contains(requiredIngredientKnowledge)) {
                            missingKnowledge = true;
                            break;
                        }

                        // Se não bloqueado pela tag, checa os itens expandidos normalmente.
                        for (ItemStack itemStack : ingredient.getItems()) {
                            Unlocks relatedUnlocks = KnowledgeHelper.getUnlockFromItem(level, itemStack);
                            Knowledge required = KnowledgeHelper.getKnowledgeFromUnlock(level, relatedUnlocks);
                            if (required != null && !playerKnowledge.contains(required)) {
                                missingKnowledge = true;
                                break;
                            }
                        }
                        if (missingKnowledge) break;
                    }

                    if (!missingKnowledge) {
                        ItemStack result = recipe.getResultItem(level.registryAccess());
                        Unlocks resultUnlocks = KnowledgeHelper.getUnlockFromItem(level, result);
                        Knowledge requiredResult = KnowledgeHelper.getKnowledgeFromUnlock(level, resultUnlocks);
                        if (requiredResult != null && !playerKnowledge.contains(requiredResult)) {
                            missingKnowledge = true;
                        }
                    }

                    if (missingKnowledge) {
                        this.recipes.remove(recipe);
                    }
                }
            }
        }
    }

    @Inject(method = "createInputSlotDefinitions", at = @At("HEAD"), cancellable = true)
    private void onCreateInputSlotDefinitions(CallbackInfoReturnable<ItemCombinerMenuSlotDefinition> cir) {
        ItemCombinerMenuSlotDefinition def = ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 8, 48, (p_266643_) -> recipes.stream().anyMatch((p_266642_) -> p_266642_.isTemplateIngredient(p_266643_))).withSlot(1, 26, 48, (p_286208_) -> recipes.stream().anyMatch((p_286206_) -> p_286206_.isBaseIngredient(p_286208_))).withSlot(2, 44, 48, (p_286207_) -> recipes.stream().anyMatch((p_286204_) -> p_286204_.isAdditionIngredient(p_286207_))).withResultSlot(3, 98, 48).build();

        cir.setReturnValue(def);
        cir.cancel();
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void knowledgeCheck(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level));
        cir.cancel();
    }

    @Inject(method = "getSlotToQuickMoveTo", at = @At("HEAD"), cancellable = true)
    private void onGetSlotToQuickMoveTo(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        int num = this.recipes.stream().map((p_266640_) -> findSlotMatchingIngredient(p_266640_, pStack)).filter(Optional::isPresent).findFirst().orElse(Optional.of(0)).get();

        cir.setReturnValue(num);
        cir.cancel();
    }

    @Shadow
    private static Optional<Integer> findSlotMatchingIngredient(SmithingRecipe pRecipe, ItemStack pStack) {
        if (pRecipe.isTemplateIngredient(pStack)) {
            return Optional.of(0);
        } else if (pRecipe.isBaseIngredient(pStack)) {
            return Optional.of(1);
        } else {
            return pRecipe.isAdditionIngredient(pStack) ? Optional.of(2) : Optional.empty();
        }
    }

    @Inject(method = "canMoveIntoInputSlots", at = @At("HEAD"), cancellable = true)
    private void onCanMoveIntoInputSlots(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        boolean canMove = this.recipes.stream().map((p_266647_) -> findSlotMatchingIngredient(p_266647_, pStack)).anyMatch(Optional::isPresent);
        cir.setReturnValue(canMove);
        cir.cancel();
    }
}
