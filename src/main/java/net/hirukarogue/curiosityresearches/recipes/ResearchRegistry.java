package net.hirukarogue.curiosityresearches.recipes;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ResearchRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CuriosityMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<ResearchRecipes>> RESEARCH_SERIALIZER =
            SERIALIZERS.register("research", () -> ResearchRecipes.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}