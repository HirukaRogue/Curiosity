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

public class SharedKnowledgeBook extends Item {
    @Nullable
    private final List<Knowledge> playerKnownKnowledge;
    @Nullable
    private final String owner;

    public SharedKnowledgeBook(Properties pProperties, List<Knowledge> playerKnownKnowledge, String owner) {
        super(pProperties);
        this.playerKnownKnowledge = playerKnownKnowledge;
        this.owner = owner;
    }

    public SharedKnowledgeBook(Properties pProperties) {
        this(pProperties, null, null);
    }

    public SharedKnowledgeBook(Properties pProperties, List<Knowledge> playerKnownKnowledge) {
        this(pProperties, playerKnownKnowledge, null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.getItemInHand(pUsedHand).shrink(1);
        pPlayer.sendSystemMessage(Component.literal("You acquire the following knowledge from " + (owner != null ? owner : "an unknown source") + ":"));
        if (playerKnownKnowledge != null) {
            for (Knowledge knowledge : playerKnownKnowledge) {
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
