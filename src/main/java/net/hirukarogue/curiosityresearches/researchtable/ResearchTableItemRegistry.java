package net.hirukarogue.curiosityresearches.researchtable;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ResearchTableItemRegistry {
    //items setup
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CuriosityMod.MOD_ID);

    //item method setup
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
