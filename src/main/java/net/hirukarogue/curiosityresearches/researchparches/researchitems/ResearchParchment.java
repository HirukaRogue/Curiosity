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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ResearchParchment extends Item {
    @Nullable
    private Knowledge knowledge = null;
    private String customName = null;

    public ResearchParchment(Properties pProperties) {
        super(pProperties);
    }

    public @org.jetbrains.annotations.Nullable Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge (@org.jetbrains.annotations.Nullable Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) {
        return customName != null ? Component.literal(customName) : knowledge != null ? Component.literal(knowledge.getKnowledge_name()) : super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (knowledge != null) {
            if (!knowledge.getKnowledge_description().isEmpty()) {
                tooltip.add(Component.literal(knowledge.getKnowledge_description()));
            }
        }
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
