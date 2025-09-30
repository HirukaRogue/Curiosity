package net.hirukarogue.curiosityresearches.events;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableEntity;
import net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CuriosityMod.MOD_ID, bus =  Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventBus {
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ResearchTableEntity.RESEARCH_TABLE_BE.get(), ResearchTableEntityRenderer::new);
    }
}
