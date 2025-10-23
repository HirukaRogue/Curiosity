package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TreasureSubEncode (
        String location,
        float chance,
        int quantity
) {

    public static final Codec<TreasureSubEncode> CODEC = RecordCodecBuilder.create( instance -> instance.group(
            Codec.STRING.fieldOf("location").forGetter(TreasureSubEncode::location),
            Codec.FLOAT.fieldOf("chance").forGetter(TreasureSubEncode::chance),
            Codec.INT.fieldOf("quantity").forGetter(TreasureSubEncode::quantity)
    ).apply(instance, TreasureSubEncode::new));
}
