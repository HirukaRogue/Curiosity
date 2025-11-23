package net.hirukarogue.curiosityresearches.data;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.recipes.ResearchCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CuriosityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ResearchCraft(packOutput));
        generator.addProvider(event.includeClient(), new ItemModelData(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new BlockStateData(packOutput, existingFileHelper));

    }

}