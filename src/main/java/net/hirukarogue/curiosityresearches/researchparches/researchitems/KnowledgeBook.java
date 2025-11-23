package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    private List<Knowledge> bookKnowledge;
    private String customName = null;

    public KnowledgeBook(Properties pProperties, List<Knowledge> bookKnowledge) {
        super(pProperties);
        this.bookKnowledge = bookKnowledge;
    }

    public KnowledgeBook(Properties pProperties) {
        this(pProperties, null);
    }

    public void setCustomName(@Nullable String name) {
        this.customName = name;
    }

    @Override
    public Component getName(ItemStack pStack) {
        return customName != null ? Component.literal(customName) : super.getName(pStack);
    }

    public void setBookKnowledges(@Nullable List<Knowledge> knowledges){
        this.bookKnowledge = knowledges != null && !knowledges.isEmpty() ? knowledges : null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.sendSystemMessage(Component.literal("You acquire the following knowledge from this book:"));
        if (bookKnowledge != null && !bookKnowledge.isEmpty()) {
            for (Knowledge knowledge : bookKnowledge) {
                if (KnowledgeHelper.playerHasKnowledge(pPlayer, knowledge)) {
                    pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level() + "(You already learned it)"));
                    continue;
                }
                pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level()));
                KnowledgeHelper.playerDiscoverKnowledge(knowledge, pPlayer);
            }
        } else {
            pPlayer.sendSystemMessage(Component.literal("- Nothing"));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
