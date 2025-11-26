package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import com.mojang.serialization.DataResult;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.KnowledgeBookData;
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
import java.util.Objects;

public class KnowledgeBook extends Item {
    public KnowledgeBook(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        KnowledgeBookData data = getKnowledgeBookRecord(pStack);
        if (data == null) {
            return super.getName(pStack);
        }
        String customName = data.customName();
        return customName != null ? Component.literal(customName) : super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        StringBuilder knowledges = new StringBuilder();
        KnowledgeBookData data = getKnowledgeBookRecord(stack);
        if (data == null) {
            tooltip.add(Component.literal("This book is empty."));
            return;
        }
        List<Knowledge> bookKnowledge = data.knowledges();
        if (bookKnowledge != null && !bookKnowledge.isEmpty()) {
            for (int i = 0; i < bookKnowledge.size(); i++) {
                if (i >= 5) {
                    knowledges.append("and ").append(bookKnowledge.size() - 3).append(" more knowledges").append("\n");
                    break;
                }
                Knowledge knowledge = bookKnowledge.get(i);
                knowledges.append("- ").append(knowledge.knowledgeName()).append(" ").append(knowledge.level()).append("\n");
            }
            // Remove last newline
            knowledges.setLength(knowledges.length() - 1);
            tooltip.add(Component.literal(knowledges.toString()));
        } else {
            tooltip.add(Component.literal("This book is empty."));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        KnowledgeBookData data = getKnowledgeBookRecord(pPlayer.getItemInHand(pUsedHand));
        if (data == null) {
            pPlayer.sendSystemMessage(Component.literal("This is a blank book. There is no knowledge to acquire."));
            return super.use(pLevel, pPlayer, pUsedHand);
        }
        List<Knowledge> bookKnowledge = data.knowledges();
        if (bookKnowledge != null && !bookKnowledge.isEmpty()) {
            pPlayer.sendSystemMessage(Component.literal("You acquire the following knowledge from this book:"));
            for (Knowledge knowledge : bookKnowledge) {
                if (KnowledgeHelper.playerHasKnowledge(pPlayer, knowledge)) {
                    pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level() + "(You already learned it)"));
                    continue;
                }
                pPlayer.sendSystemMessage(Component.literal("- "+ knowledge.knowledgeName() + " " + knowledge.level()));
                KnowledgeHelper.playerDiscoverKnowledge(knowledge, pPlayer);
            }
        } else {
            pPlayer.sendSystemMessage(Component.literal("This is a blank book. There is no knowledge to acquire."));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    public static KnowledgeBookData getKnowledgeBookRecord(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            if (itemStack.getTag().contains("KnowledgeBookRecord")) {
                CompoundTag kbTag = itemStack.getTag().getCompound("KnowledgeBookRecord");
                DataResult<KnowledgeBookData> decode = KnowledgeBookData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, kbTag);
                return decode.result().orElse(null);
            }
        }

        return null;
    }

    public static void setKnowledgeBookRecord(ItemStack itemStack, KnowledgeBookData knowledgeBookData) {
        KnowledgeBookData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, knowledgeBookData).resultOrPartial(msg -> CuriosityMod.LOGGER.warn("Failed loading knowledge book encode: " + msg)).ifPresent(tag -> {
            CompoundTag itemTag = itemStack.getOrCreateTag();
            itemTag.put("KnowledgeBookRecord", tag);
        });
    }
}
