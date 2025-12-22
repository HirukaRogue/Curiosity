package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import com.mojang.serialization.DataResult;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.hirukarogue.curiosityresearches.records.ResearchParchmentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import java.util.Objects;

public class ResearchParchment extends Item {
    public ResearchParchment(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public Component getName(ItemStack pStack) {
        ResearchParchmentData rpRecord = ResearchParchmentData.load(pStack);
        if (rpRecord == null) {
            return super.getName(pStack);
        }

        return rpRecord.customName() != null ? Component.literal(rpRecord.customName()) : super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Knowledge knowledge = null;
        ResearchParchmentData rpRecord = ResearchParchmentData.load(stack);
        if (rpRecord == null) {
            return;
        }

        String knowledgeKey = rpRecord.knowledgeKey();

        if (knowledgeKey != null) {
            try {
                List<Knowledge> kList = world.registryAccess().registry(CuriosityMod.KNOWLEDGE_REGISTRY).get().stream().toList();
                for (Knowledge k : kList) {
                    if (k.key().equals(knowledgeKey)){
                        knowledge = k;
                        break;
                    }
                }
            } catch (Exception ignore) {
                return;
            }
        }

        if (knowledge == null) {
            return;
        }

        StringBuilder researchDescription = new StringBuilder();
        List<String> descLines = knowledge.knowledgeDescription();
        for (int i = 0; i < descLines.size(); i++) {
            researchDescription.append(descLines.get(i));
            if (i < descLines.size() - 1) {
                researchDescription.append("\n");
            }
        }
        tooltip.add(Component.literal(researchDescription.toString()));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ResearchParchmentData rpRecord = ResearchParchmentData.load(pPlayer.getItemInHand(pUsedHand));
        if (rpRecord != null) {
            Knowledge knowledge = null;
            try {
                List<Knowledge> knowledgeList = pLevel.registryAccess().registry(CuriosityMod.KNOWLEDGE_REGISTRY).get().stream().toList();
                String kKey = rpRecord.knowledgeKey() != null ? rpRecord.knowledgeKey() : null;
                for (Knowledge k : knowledgeList) {
                    if (k.key().equals(kKey)) {
                        knowledge = k;
                        break;
                    }
                }

                if (knowledge == null) {
                    pPlayer.sendSystemMessage(Component.literal("You learned Nothing!"));
                    pPlayer.getItemInHand(pUsedHand).shrink(1);
                    return super.use(pLevel, pPlayer, pUsedHand);
                }

                for (Knowledge k : knowledgeList) {
                    Unlocks unlocks = KnowledgeHelper.getKnowledgeUnlock(pLevel, k);
                    assert unlocks != null;
                    for (ResourceLocation rl : unlocks.unlocks()) {
                        if (pLevel.registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).get(rl) == null) {
                            continue;
                        }
                        if (Objects.equals(pLevel.registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).get(rl), knowledge) && !KnowledgeHelper.playerHasKnowledge(pPlayer, k)) {
                            pPlayer.sendSystemMessage(Component.literal("To discover this you require " + k.knowledgeName() + " " + k.level() + "!"));
                            return super.use(pLevel, pPlayer, pUsedHand);
                        }
                    }
                }

                if (knowledge.level() > 1) {
                    Knowledge previousKnowledge = null;
                    for (Knowledge k : knowledgeList) {
                        if (k.knowledgeName().equals(knowledge.knowledgeName()) && k.level() == knowledge.level() - 1) {
                            previousKnowledge = k;
                            break;
                        }
                    }
                    if (previousKnowledge != null && !KnowledgeHelper.playerHasKnowledge(pPlayer, previousKnowledge)) {
                        pPlayer.sendSystemMessage(Component.literal("To discover this you must first learn " + previousKnowledge.knowledgeName() + " " + previousKnowledge.level() + "!"));
                        return super.use(pLevel, pPlayer, pUsedHand);
                    }
                }
            } catch (Exception e) {
                CuriosityMod.LOGGER.warn("Failed to get knowledge from research parchment: " + e.getMessage());
                pPlayer.sendSystemMessage(Component.literal("You learned Nothing!"));
                pPlayer.getItemInHand(pUsedHand).shrink(1);
                return super.use(pLevel, pPlayer, pUsedHand);
            }

            pPlayer.sendSystemMessage(Component.literal("You learned " + knowledge.knowledgeName() + " " + knowledge.level() + "!"));
            pPlayer.getItemInHand(pUsedHand).shrink(1);
            KnowledgeHelper.playerDiscoverKnowledge(knowledge, pPlayer);
        } else {
            pPlayer.sendSystemMessage(Component.literal("You learned Nothing!"));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
