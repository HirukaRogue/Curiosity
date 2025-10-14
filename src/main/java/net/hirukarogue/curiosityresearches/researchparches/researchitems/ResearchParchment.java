package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ResearchParchment extends Item {
    @Nullable
    public final Knowledge knowledge;

    public ResearchParchment(Properties pProperties) {
        super(pProperties);
        this.knowledge = null;
    }

    public ResearchParchment(Properties pProperties, Knowledge knowledge) {
        super(pProperties);
        this.knowledge = knowledge;
    }

    public @org.jetbrains.annotations.Nullable Knowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.getItemInHand(pUsedHand).shrink(1);
        if (knowledge != null) {
            pPlayer.sendSystemMessage(Component.literal("You acquire knowledge of " + knowledge.getKnowledge_name() + "!"));
            KnowledgeData data = new KnowledgeData(pLevel);
            data.addKnowledgeToPlayer(pPlayer, knowledge);
        } else {
            pPlayer.sendSystemMessage(Component.literal("You acquired knowledge of None!"));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
