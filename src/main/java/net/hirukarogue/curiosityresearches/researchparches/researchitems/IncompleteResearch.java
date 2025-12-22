package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.ResearchParchmentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class IncompleteResearch extends Item {
    public IncompleteResearch(Properties pProperties) {
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

        StringBuilder knowledgeText = new StringBuilder();
        List<String> description = knowledge.knowledgeDescription();

        for (int i = 0; i < description.size(); i++) {
            String line = description.get(i);
            knowledgeText.append(line);
            if (i < description.size() - 1) {
                knowledgeText.append("\n");
            }
        }
        tooltip.add(Component.literal(knowledgeText.toString()));
    }
}
