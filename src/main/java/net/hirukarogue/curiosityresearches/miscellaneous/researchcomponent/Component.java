package net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class Component {
    private final Either<Item, TagKey<Item>> component;
    private final int quantity;
    private final Boolean consume;

    public static final Codec<Either<Item, TagKey<Item>>> SUB_CODEC = Codec.either(
            ForgeRegistries.ITEMS.getCodec(),
            TagKey.codec(ForgeRegistries.ITEMS.getRegistryKey())
    );

    public static final Codec<Component> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SUB_CODEC.fieldOf("component").forGetter(Component::getComponent),
            Codec.INT.fieldOf("quantity").forGetter(Component::getQuantity),
            Codec.BOOL.fieldOf("consume").forGetter(Component::consumeComponent)
    ).apply(instance, Component::new));

    private int getQuantity() {
        return this.quantity;
    }

    public Component(Either<Item,TagKey<Item>> component, int quantity, boolean consume) {
        if (quantity < 1) {
            CuriosityMod.LOGGER.warn("A component has a quantity less than 1. This is not allowed. Setting it to 1.");
            quantity = 1;
        }
        this.component = component;
        this.quantity = quantity;
        this.consume = consume;
    }

    public Component(ItemStack component, boolean consume) {
        this.component = Either.left(component.getItem());
        this.quantity = component.getCount();
        this.consume = consume;
    }

    public Component(ItemStack component) {
        this.component = Either.left(component.getItem());
        this.quantity = component.getCount();
        this.consume = false;
    }

    public Either<Item, TagKey<Item>> getComponent() {
        return component;
    }

    public int getNeededQuantity() {
        return quantity;
    }

    public boolean consumeComponent() {
        return consume;
    }

    public boolean isEmpty() {
        return component.right().isEmpty() && component.left().isEmpty();
    }

    @Override
    public String toString() {
        return "Component{" +
                "component=" + component +
                ", quantity=" + quantity +
                ", consume=" + consume +
                '}';
    }
}
