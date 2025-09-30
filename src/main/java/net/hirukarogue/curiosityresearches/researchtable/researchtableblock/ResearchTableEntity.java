package net.hirukarogue.curiosityresearches.researchtable.researchtableblock;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ResearchTableEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CuriosityMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ResearchTableBlockEntity>>RESEARCH_TABLE_BE =
            BLOCK_ENTITIES.register("research_table_be", () ->
                    BlockEntityType.Builder.of(ResearchTableBlockEntity::new,
                            ResearchTableRegistry.RESEARCH_TABLE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
