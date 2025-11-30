package net.hirukarogue.curiosityresearches.events;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CuriosityMod.MOD_ID, bus =  Mod.EventBusSubscriber.Bus.FORGE)
public class LockRecipes {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack output = event.getCrafting();
        ResourceLocation recipeId = getRecipeId(output, player);

        List<Knowledge> playerKnowledges = KnowledgeHelper.getPlayerKnowledge(player);
        List<Knowledge> encyclopedia = player.level().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).stream().toList();
        List<Unlocks> unlocks = player.level().registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY).stream().toList();
        boolean hasKnowledge = false;

        Recipe<?> recipe = player.level().getRecipeManager().byKey(recipeId).orElse(null);

        if (recipe != null) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            Knowledge requiredKnowledge = null;
            if (!ingredients.isEmpty()) {
                boolean found = false;
                for (Unlocks unlock : unlocks) {
                    for (ResourceLocation rl : unlock.unlocks()) {
                        if (ingredients.contains(Ingredient.of(new ItemStack(BuiltInRegistries.ITEM.get(rl))))) {
                            requiredKnowledge = encyclopedia.stream()
                                    .filter(knowledge -> knowledge.key().equals(unlock.knowledgeName()+"_"+unlock.level()))
                                    .findFirst()
                                    .orElse(null);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }

            if (playerKnowledges.contains(requiredKnowledge)) {
                hasKnowledge = true;
            }
        } else {
            hasKnowledge = true; // Allow crafting if recipe is not found
        }

        if (!hasKnowledge) {
            // Revoke the crafting
            event.getInventory().removeItem(output.getCount(), output.getCount());
        }
    }

    private static ResourceLocation getRecipeId(ItemStack stack, Player player) {
        RecipeManager recipeManager = player.level().getRecipeManager();
        RegistryAccess registryAccess = player.level().registryAccess();

        return recipeManager.getRecipes().stream()
                .filter(recipe -> {
                            ItemStack output = recipe.getResultItem(registryAccess);
                            return !output.isEmpty() && ItemStack.isSameItem(output, stack);
                        })
                .map(recipe -> recipe.getId())
                .findFirst()
                .orElse(null);
    }
}
