package net.hirukarogue.curiosityresearches.ltfunctions;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.ltfunctions.setknowledge.SetKnowledge;
import net.hirukarogue.curiosityresearches.ltfunctions.setknowledge.SetRandomKnowledge;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CuriosityLootItemFunctions extends LootItemFunctions {
    public static final DeferredRegister<LootItemFunctionType> FUNC_REG = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CuriosityMod.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> SET_RANDOM_KNOWLEDGE = FUNC_REG.register("set_random_knowledge",
            () -> new LootItemFunctionType(new SetRandomKnowledge.Serializer()));

    public static final RegistryObject<LootItemFunctionType> SET_KNOWLEDGE = FUNC_REG.register("set_knowledge",
            () -> new LootItemFunctionType(new SetKnowledge.Serializer()));

    private static Supplier<LootItemFunctionType> type(Serializer<? extends LootItemFunction> serializer) {
        return ()-> new LootItemFunctionType(serializer);
    }

    public static void register(IEventBus eventBus) {
        FUNC_REG.register(eventBus);
    }

}
