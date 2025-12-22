package net.hirukarogue.curiosityresearches.mixin;

import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.List;

@Mixin(Slot.class)
public abstract class SlotLock {
    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void onMayPickup(Player player, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot)(Object)this;
        if (self instanceof ResultSlot) {
            List<Knowledge> playerKnowledge = KnowledgeHelper.getPlayerKnowledge(player);
            List<Unlocks> unlocks = player.level().registryAccess().registryOrThrow(net.hirukarogue.curiosityresearches.CuriosityMod.UNLOCK_REGISTRY).stream().toList();

            for (Unlocks unlock : unlocks) {
                for (ResourceLocation rl : unlock.unlocks()) {
                    Container container = self.container;
                    if (container != null) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            if (container.getItem(i).getItem().builtInRegistryHolder().key().location().equals(rl)) {
                                Knowledge requiredKnowledge = player.level().registryAccess().registryOrThrow(net.hirukarogue.curiosityresearches.CuriosityMod.KNOWLEDGE_REGISTRY)
                                        .stream()
                                        .filter(knowledge -> knowledge.key().equals(unlock.knowledgeName() + "_" + unlock.level()))
                                        .findFirst()
                                        .orElse(null);
                                if (requiredKnowledge != null && !playerKnowledge.contains(requiredKnowledge)) {
                                    cir.setReturnValue(false);
                                    if (!player.level().isClientSide() && player.containerMenu.getCarried().isEmpty()) {
                                        MutableComponent itemName = this.getItem().getHoverName().copy().withStyle(ChatFormatting.AQUA);
                                        MutableComponent knowledgeRequiredString = Component.literal(requiredKnowledge.knowledgeName() + " " + requiredKnowledge.level()).withStyle(ChatFormatting.GOLD);

                                        player.displayClientMessage(Component.literal("To craft ").append(itemName)
                                                .append(Component.literal(", you need "))
                                                .append(knowledgeRequiredString), true);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
