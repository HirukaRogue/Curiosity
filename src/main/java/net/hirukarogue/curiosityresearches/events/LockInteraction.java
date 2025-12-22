package net.hirukarogue.curiosityresearches.events;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = CuriosityMod.MOD_ID, bus =  Mod.EventBusSubscriber.Bus.FORGE)
public class LockInteraction {
    @SubscribeEvent
    public static void onBlockInteraction(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Block block = level.getBlockState(event.getPos()).getBlock();

        Optional<TagKey<Item>> tags = block.asItem().builtInRegistryHolder().tags().findFirst();

        ItemStack blockItemStack = new ItemStack(block.asItem());
        Unlocks unlocks = KnowledgeHelper.getUnlockFromItem(level, blockItemStack);
        Knowledge knowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, unlocks);

        if (unlocks != null && knowledge != null) {
            if (!KnowledgeHelper.playerHasKnowledge(event.getEntity(), knowledge)) {
                event.getEntity().sendSystemMessage(Component.literal("You don't have knowledge to interact with this block."));
                event.setCanceled(true);
            }
        }

        if (tags.isPresent()) {
            unlocks = KnowledgeHelper.getUnlockFromItem(level, tags.get());
            knowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, unlocks);

            if (unlocks != null && knowledge != null) {
                if (!KnowledgeHelper.playerHasKnowledge(event.getEntity(), knowledge)) {
                    event.getEntity().sendSystemMessage(Component.literal("You don't have knowledge to interact with this block."));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemStackInteraction(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        ItemStack itemStack = event.getItemStack();

        Optional<TagKey<Item>> tags = itemStack.getItem().builtInRegistryHolder().tags().findFirst();

        Unlocks unlocks = KnowledgeHelper.getUnlockFromItem(level, itemStack);
        Knowledge knowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, unlocks);

        if (unlocks != null && knowledge != null) {
            if (!KnowledgeHelper.playerHasKnowledge(event.getEntity(), knowledge)) {
                event.getEntity().sendSystemMessage(Component.literal("You don't have knowledge to interact with this item."));
                event.setCanceled(true);
            }
        }

        if (tags.isPresent()) {
            unlocks = KnowledgeHelper.getUnlockFromItem(level, tags.get());
            knowledge = KnowledgeHelper.getKnowledgeFromUnlock(level, unlocks);
            if (unlocks != null && knowledge != null) {
                if (!KnowledgeHelper.playerHasKnowledge(event.getEntity(), knowledge)) {
                    event.getEntity().sendSystemMessage(Component.literal("You don't have knowledge to interact with this item."));
                    event.setCanceled(true);
                }
            }
        }
    }
}
