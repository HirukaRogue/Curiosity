package net.hirukarogue.curiosityresearches.miscellaneous.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ResearchJsonCompiler {
    private static final Codec<Map<String, Component>> COMPONENTS_CODEC = Codec.unboundedMap(Codec.STRING, Component.CODEC);

    public static NonNullList<Component> compile(JsonObject pSerializedRecipe) {
        JsonObject compiler = GsonHelper.getAsJsonObject(pSerializedRecipe, "keys");
        Map<String,Component> componentReference = COMPONENTS_CODEC.parse(JsonOps.INSTANCE, compiler).result().orElse(Map.of());

        COMPONENTS_CODEC.parse(JsonOps.INSTANCE, compiler).error().ifPresent(
                error -> CuriosityMod.LOGGER.error("Component parse error: " + error.message())
        );

        JsonArray compiler2 = GsonHelper.getAsJsonArray(pSerializedRecipe, "pattern");
        List<String> pattern = Codec.STRING.listOf().parse(JsonOps.COMPRESSED,compiler2).result().orElse(List.of());

        if (pattern.size() != 3) {
            throw new RuntimeException("The pattern don't fit");
        }

        if (pattern.get(0).length() != 2) {
            throw new RuntimeException("The first line should always be 2 elements, including space for empty slots");
        }
        if (pattern.get(1).length() != 3) {
            throw new RuntimeException("The second line should always be 3 elements, including space for empty slots");
        }
        if (pattern.get(2).length() != 2) {
            throw new RuntimeException("The third line should always be 2 elements, including space for empty slots");
        }

        NonNullList<Component> components = NonNullList.withSize(7, new Component(ItemStack.EMPTY));

        byte index = 0;

        for (String line : pattern) {
            String[] characters = line.split("");
            for (String character : characters) {
                if (Objects.equals(character, " ")) {
                    index++;
                    continue;
                }

                if (!componentReference.containsKey(character)) {
                    throw new RuntimeException("You didn't registered this key");
                }

                components.set(index, componentReference.get(character));

                index++;
            }
        }

        return components;
    }
}
