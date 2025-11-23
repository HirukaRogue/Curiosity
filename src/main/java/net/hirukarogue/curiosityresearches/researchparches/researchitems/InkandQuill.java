package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class InkandQuill extends Item {
    public InkandQuill(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        if (pPlayer.getItemInHand(InteractionHand.MAIN_HAND).is(this) && pPlayer.getItemInHand(InteractionHand.OFF_HAND).is(Items.PAPER)) {
            pPlayer.sendSystemMessage(Component.literal("You wrote new research notes!"));
            pPlayer.getItemInHand(InteractionHand.OFF_HAND).shrink(1);
            pPlayer.addItem(new ItemStack(ResearchItemsRegistry.RESEARCH_NOTES.get()));
            pPlayer.getItemInHand(pUsedHand).hurt(1, Objects.requireNonNull(pPlayer).getRandom(), null);
            if (pPlayer.getItemInHand(pUsedHand).getDamageValue() >= pPlayer.getItemInHand(pUsedHand).getMaxDamage()) {
                pPlayer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get()));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
