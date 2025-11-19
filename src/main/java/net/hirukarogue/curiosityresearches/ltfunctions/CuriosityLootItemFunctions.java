package net.hirukarogue.curiosityresearches.ltfunctions;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.ltfunctions.setknowledge.SetRandomKnowledge;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CuriosityLootItemFunctions extends LootItemFunctions {
    DeferredRegister<LootItemFunctionType> FUNC_REG = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CuriosityMod.MOD_ID);

    RegistryObject<LootItemFunctionType> SET_RANDOM_KNOWLEDGE = FUNC_REG.register("set_random_knowledge",
            () -> new LootItemFunctionType(new SetRandomKnowledge.Serializer()));

    private static Supplier<LootItemFunctionType> type(Serializer<? extends LootItemFunction> serializer) {
        return ()-> new LootItemFunctionType(serializer);
    }
}
