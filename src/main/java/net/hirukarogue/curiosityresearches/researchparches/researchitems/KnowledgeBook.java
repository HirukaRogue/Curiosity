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
import java.util.List;

public class KnowledgeBook extends Item {
    @Nullable
    private final List<Knowledge> bookKnowledge;

    public KnowledgeBook(Properties pProperties, List<Knowledge> bookKnowledge) {
        super(pProperties);
        this.bookKnowledge = bookKnowledge;
    }

    public KnowledgeBook(Properties pProperties) {
        this(pProperties, null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.getItemInHand(pUsedHand).shrink(1);
        pPlayer.sendSystemMessage(Component.literal("You acquire the following knowledge from this book:"));
        if (bookKnowledge != null) {
            for (Knowledge knowledge : bookKnowledge) {
                pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.getKnowledge_name()));
                KnowledgeData data = new KnowledgeData(pLevel);
                data.addKnowledgeToPlayer(pPlayer, knowledge);
            }
        } else {
            pPlayer.sendSystemMessage(Component.literal("- None"));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
