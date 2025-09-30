package net.hirukarogue.curiosityresearches.mixin;

import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;

@Mixin(SmithingMenu.class)
public abstract class UnlockSmithingMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private List<SmithingRecipe> recipes;


    public UnlockSmithingMixin(@Nullable MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pType, pContainerId, pPlayerInventory, pAccess);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        Player player = this.player;

        KnowledgeData data = new KnowledgeData(player.level());
        List<Knowledge> allKnowledge = data.loadCompleteEncyclopedia();
        List<Knowledge> playerKnowledge = data.loadPlayerKnowledge(player);

        for (int i = 0; i < this.recipes.size(); i++) {
            for (Knowledge knowledge : allKnowledge) {
                if (knowledge.getUnlocks() != null && knowledge.getUnlocks().getUnlocked_recipes() != null) {
                    if (knowledge.getUnlocks().getUnlocked_recipes().contains(this.recipes.get(i))) {
                        if (!playerKnowledge.contains(knowledge)) {
                            this.recipes.remove(i);
                        }
                    }
                }
            }
        }
    }


}
