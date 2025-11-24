package net.hirukarogue.curiosityresearches;

import com.mojang.logging.LogUtils;
import net.hirukarogue.curiosityresearches.ltfunctions.CuriosityLootItemFunctions;
import net.hirukarogue.curiosityresearches.recipes.ResearchRegistry;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.*;
import net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableEntity;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.ResearchMenuScreen;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.ResearchMenuType;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CuriosityMod.MOD_ID)
public class CuriosityMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "curiosity_researches";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Registry<Knowledge>> KNOWLEDGE_REGISTRY =
            ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "knowledge"));

    public static final ResourceKey<Registry<Unlocks>> UNLOCK_REGISTRY =
            ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "unlocks"));

    public CuriosityMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //creative tab
        CuriosityCreativeTabs.register(modEventBus);

        //Research Table
        ResearchTableRegistry.register(modEventBus);
        ResearchTableItemRegistry.register(modEventBus);
        ResearchTableEntity.register(modEventBus);
        ResearchMenuType.register(modEventBus);

        //Research Items
        ResearchItemsRegistry.register(modEventBus);

        //Research Recipes
        ResearchRegistry.register(modEventBus);

        //Loot Item Functions
        CuriosityLootItemFunctions.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(ResearchMenuType.RESEARCH_MENU.get(), ResearchMenuScreen::new);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class GeneralModEvents {
        @SubscribeEvent
        public static void addRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(
                    KNOWLEDGE_REGISTRY,
                    Knowledge.CODEC,
                    Knowledge.CODEC
            );
            event.dataPackRegistry(
                    UNLOCK_REGISTRY,
                    Unlocks.CODEC,
                    Unlocks.CODEC
            );
        }
    }
}
