package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import com.mojang.serialization.DataResult;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.SharedKnowledgeBookData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SharedKnowledgeBook extends Item {
    public SharedKnowledgeBook(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        List<Knowledge> playerKnowledge = KnowledgeHelper.getPlayerKnowledge(pPlayer);
        String sharedBy = pPlayer.getDisplayName().getString();
        SharedKnowledgeBookData.save(pStack, new SharedKnowledgeBookData(playerKnowledge, sharedBy));
        super.onCraftedBy(pStack, pLevel, pPlayer);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        StringBuilder knowledges = new StringBuilder();
        SharedKnowledgeBookData record = SharedKnowledgeBookData.load(stack);
        if (record == null) {
            tooltip.add(Component.literal("This book is empty."));
            return;
        }

        List<Knowledge> bookKnowledge = record.knowledges();
        knowledges.append("owner: ").append(record.sharedBy() != null ? record.sharedBy() : "an unknown source").append("\n");
        if (bookKnowledge != null && !bookKnowledge.isEmpty()) {
            for (int i = 0; i < bookKnowledge.size(); i++) {
                if (i >= 5) {
                    knowledges.append("and ").append(bookKnowledge.size() - 3).append(" more knowledges");
                    break;
                }
                Knowledge knowledge = bookKnowledge.get(i);
                knowledges.append("- ").append(knowledge.knowledgeName()).append(" ").append(knowledge.level());
                if (i < bookKnowledge.size() - 1) {
                    knowledges.append("\n");
                }
            }
            tooltip.add(Component.literal(knowledges.toString()));
        } else {
            tooltip.add(Component.literal("This book is empty."));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        SharedKnowledgeBookData record = SharedKnowledgeBookData.load(pPlayer.getItemInHand(pUsedHand));
        pPlayer.getItemInHand(pUsedHand).shrink(1);
        if (record == null) {
            pPlayer.sendSystemMessage(Component.literal("This is a blank book. There is no knowledge to acquire."));
            return super.use(pLevel, pPlayer, pUsedHand);
        }
        List<Knowledge> playerKnownKnowledge = record.knowledges();
        String owner = record.sharedBy();
        pPlayer.sendSystemMessage(Component.literal("You acquire the following knowledge from " + (owner != null && owner.equals(pPlayer.getDisplayName().getString()) ? "Yourself" : owner != null ? owner : "an unknown source") + ":"));
        if (playerKnownKnowledge != null && !playerKnownKnowledge.isEmpty()) {
            for (Knowledge knowledge : playerKnownKnowledge) {
                if (KnowledgeHelper.playerHasKnowledge(pPlayer, knowledge)) {
                    pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level() + " (You already learned it)"));
                    continue;
                }
                pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level()));
                KnowledgeHelper.playerDiscoverKnowledge(knowledge, pPlayer);
            }
        } else {
            pPlayer.sendSystemMessage(Component.literal("- None"));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
