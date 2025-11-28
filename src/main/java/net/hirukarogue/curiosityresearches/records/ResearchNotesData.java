package net.hirukarogue.curiosityresearches.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.hirukarogue.curiosityresearches.CuriosityMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record ResearchNotesData(
        @Nullable ResourceLocation target,
        @Nullable Coordinates coordinates,
        @Nullable String customName,
        @Nullable List<String> note
) {
    // Codec limpo e puro – aceita qualquer ResourceLocation válido
    public static final Codec<ResearchNotesData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.optionalFieldOf("target")
                    .forGetter(r -> Optional.ofNullable(r.target())),

            Coordinates.CODEC.optionalFieldOf("coordinates")
                    .forGetter(r -> Optional.ofNullable(r.coordinates())),

            Codec.STRING.optionalFieldOf("custom_name")
                    .forGetter(r -> Optional.ofNullable(r.customName())),

            Codec.STRING.listOf().optionalFieldOf("note")
                    .forGetter(r -> Optional.ofNullable(r.note()))
    ).apply(inst, (target, coordinates, customName, note) -> new ResearchNotesData(target.orElse(null), coordinates.orElse(null), customName.orElse(null), note.orElse(null)))); // <-- constructor reference = mais limpo

    // Coordinates (sem mudança)
    public record Coordinates(
            Optional<Double> x, Optional<Double> x2,
            Optional<Double> y, Optional<Double> y2,
            Optional<Double> z, Optional<Double> z2
    ) {
        public static final Codec<Coordinates> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.optionalFieldOf("x").forGetter(Coordinates::x),
                Codec.DOUBLE.optionalFieldOf("x2").forGetter(Coordinates::x2),
                Codec.DOUBLE.optionalFieldOf("y").forGetter(Coordinates::y),
                Codec.DOUBLE.optionalFieldOf("y2").forGetter(Coordinates::y2),
                Codec.DOUBLE.optionalFieldOf("z").forGetter(Coordinates::z),
                Codec.DOUBLE.optionalFieldOf("z2").forGetter(Coordinates::z2)
        ).apply(inst, Coordinates::new));
    }

    // Load / Save (sem alteração)
    public static @Nullable ResearchNotesData load(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("research_notes")) {
            return CODEC.decode(NbtOps.INSTANCE, tag.getCompound("research_notes"))
                    .resultOrPartial(CuriosityMod.LOGGER::error)
                    .map(pair -> pair.getFirst())
                    .orElse(null);
        }
        return null;
    }

    public static void save(ItemStack stack, ResearchNotesData data) {
        CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(CuriosityMod.LOGGER::error)
                .ifPresent(encoded -> stack.getOrCreateTag().put("research_notes", encoded));
    }
}