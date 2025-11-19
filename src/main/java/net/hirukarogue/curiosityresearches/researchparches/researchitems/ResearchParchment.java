package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ResearchParchment extends Item {
    @Nullable
    private String knowledge = null;
    private String customName = null;

    public ResearchParchment(Properties pProperties) {
        super(pProperties);
    }

    public @org.jetbrains.annotations.Nullable String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge (@Nullable String knowledge) {
        this.knowledge = knowledge;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) {
        return customName != null ? Component.literal(customName) : super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Knowledge knowledge = null;

        if (this.knowledge != null) {
            try {
                List<Knowledge> kList = world.registryAccess().registry(CuriosityMod.KNOWLEDGE_REGISTRY).get().stream().toList();
                for (Knowledge k : kList) {
                    if (k.key().equals(this.knowledge)){
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
        tooltip.add(Component.literal(knowledge.knowledgeDescription()));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (knowledge != null) {
            Knowledge knowledge = null;
            try {
                List<Knowledge> knowledgeList = pLevel.registryAccess().registry(CuriosityMod.KNOWLEDGE_REGISTRY).get().stream().toList();
                for (Knowledge k : knowledgeList) {
                    if (k.key().equals(this.knowledge)) {
                        knowledge = k;
                        break;
                    }
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

                if (knowledge == null) {
                    pPlayer.sendSystemMessage(Component.literal("You learned Nothing!"));
                    pPlayer.getItemInHand(pUsedHand).shrink(1);
                    return super.use(pLevel, pPlayer, pUsedHand);
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
