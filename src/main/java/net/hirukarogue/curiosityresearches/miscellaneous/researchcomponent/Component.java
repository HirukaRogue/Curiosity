package net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;

import java.util.Optional;

public class Component {
    private final Either<Item, TagKey<Item>> component;
    @Nullable private final String knowledgeKey;
    @Nullable private final ResourceLocation noteLocation;
    private final int quantity;
    private final boolean consume;

    public static final Codec<Either<Item, TagKey<Item>>> SUB_CODEC = Codec.either(
            ForgeRegistries.ITEMS.getCodec(),
            TagKey.codec(ForgeRegistries.ITEMS.getRegistryKey())
    );

    public static final Codec<Component> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SUB_CODEC.fieldOf("component").forGetter(c -> c.component),

            Codec.STRING.optionalFieldOf("knowledge_key")
                    .forGetter(c -> Optional.ofNullable(c.knowledgeKey)),

            ResourceLocation.CODEC.optionalFieldOf("note_location")
                    .forGetter(c -> Optional.ofNullable(c.noteLocation)), // se for String → ResourceLocation

            Codec.INT.fieldOf("quantity").forGetter(c -> c.quantity),
            Codec.BOOL.fieldOf("consume").forGetter(c -> c.consume)
    ).apply(instance, Component::new));

    public Component(Either<Item, TagKey<Item>> component,
                     Optional<String> knowledgeKeyOpt,
                     Optional<ResourceLocation> noteLocationOpt,
                     int quantity,
                     boolean consume) {

        if (quantity < 1) {
            CuriosityMod.LOGGER.warn("A component has a quantity less than 1. Setting it to 1.");
            quantity = 1;
        }

        this.component = component;
        this.quantity = quantity;
        this.consume = consume;

        // Validação do knowledge_key
        if (knowledgeKeyOpt.isPresent()) {
            if (component.left().isEmpty()) {
                throw new IllegalArgumentException("knowledge_key is reserved for Incomplete Research and cannot be used with tag components");
            }
            Item item = component.left().get();
            if (!item.equals(ResearchItemsRegistry.INCOMPLETE_RESEARCH.get())) {
                throw new IllegalArgumentException("knowledge_key is reserved for Incomplete Research and cannot be used with other items");
            }
            this.knowledgeKey = knowledgeKeyOpt.get();
        } else {
            this.knowledgeKey = null;
        }

        // Validação do note_location
        if (noteLocationOpt.isPresent()) {
            if (component.left().isEmpty()) {
                throw new IllegalArgumentException("note_location is reserved for Research Notes and cannot be used with tag components");
            }
            Item item = component.left().get();
            if (!item.equals(ResearchItemsRegistry.RESEARCH_NOTES.get())) {
                throw new IllegalArgumentException("note_location is reserved for Research Notes and cannot be used with other items");
            }
            this.noteLocation = noteLocationOpt.get(); // ou guardar como ResourceLocation se preferir
        } else {
            this.noteLocation = null;
        }
    }

    // Construtores convenientes
    public Component(ItemStack stack, boolean consume) {
        this(Either.left(stack.getItem()), Optional.empty(), Optional.empty(), stack.getCount(), consume);
    }

    public Component(ItemStack stack) {
        this(stack, false);
    }

    public Either<Item, TagKey<Item>> getComponent() {
        return component;
    }

    @Nullable
    public String getKnowledgeKey() {
        return knowledgeKey;
    }

    @Nullable
    public ResourceLocation getNoteLocation() {
        return noteLocation;
    }

    public int getNeededQuantity() {
        return quantity;
    }

    public boolean consumeComponent() {
        return consume;
    }

    public boolean isEmpty() {
        return component.left().isEmpty() && component.right().isEmpty();
    }

    @Override
    public String toString() {
        return this.knowledgeKey == null ? "Component{" +
                "component=" + component +
                ", quantity=" + quantity +
                ", consume=" + consume +
                '}' : "Component{" +
                "component=" + component +
                ", knowledgeKey='" + knowledgeKey + '\'' +
                ", quantity=" + quantity +
                ", consume=" + consume +
                '}';
    }
}
