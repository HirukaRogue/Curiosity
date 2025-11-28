package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.records.ResearchNotesData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;

public class ResearchNotes extends Item {
    public ResearchNotes(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        ResearchNotesData data = ResearchNotesData.load(pStack);
        if (data == null) {
            return super.getName(pStack);
        }

        if (data.customName() != null && !data.customName().isEmpty()) {
            return Component.literal(data.customName());
        }

        return super.getName(pStack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        ResearchNotesData data = ResearchNotesData.load(pStack);
        if (data == null) {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
            return;
        }
        if (data.note() != null && !data.note().isEmpty()) {
            StringBuilder noteText = new StringBuilder();
            List<String> noteStrings = data.note();
            for (String noteLine : noteStrings) {
                noteText.append(noteLine).append("\n");
            }
            // Remove last newline
            noteText.setLength(noteText.length() - 1);
            pTooltipComponents.add(Component.literal(noteText.toString()));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
