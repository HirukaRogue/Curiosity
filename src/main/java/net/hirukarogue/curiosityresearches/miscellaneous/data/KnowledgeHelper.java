package net.hirukarogue.curiosityresearches.miscellaneous.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.Knowledge.Unlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class KnowledgeHelper {
    private static Path getPathPlayer(Player player) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            Path worldRoot = server.getWorldPath(LevelResource.ROOT);

            return worldRoot.resolve("curiosityresearches").resolve("playerdata").resolve(player.getUUID() + "_knowledge.json");
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSingleplayerServer() != null) {
                Path worldRoot = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);

                return worldRoot.resolve("curiosityresearches").resolve("playerdata").resolve(player.getUUID() + "_knowledge.json");
            } else {
                throw new RuntimeException("Player Knowledge can only be initialized in a server or singleplayer context.");
            }
        }
    }

    public static List<Knowledge> getPlayerKnowledge(Player player) {
        File playerFile = getPathPlayer(player).toFile();
        if (!playerFile.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(playerFile.toPath())) {
            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            List<Knowledge> knowledgeList = new ArrayList<>();
            Registry<Knowledge> registry = player.level().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY);

            for (String key : jsonObject.keySet()) {
                ResourceLocation rl = new ResourceLocation(key);
                Knowledge knowledge = registry.get(rl);
                if (knowledge != null) {
                    knowledgeList.add(knowledge);
                }
            }

            return knowledgeList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean playerHasKnowledge(Player player, Knowledge knowledge) {
        return getPlayerKnowledge(player).contains(knowledge);
    }

    public static void playerDiscoverKnowledge(Knowledge k, Player player) {
        List<Knowledge> currentKnowledge = getPlayerKnowledge(player);
        if (currentKnowledge.contains(k)) {
            return;
        }

        currentKnowledge.add(k);
        File playerFile = getPathPlayer(player).toFile();
        playerFile.getParentFile().mkdirs();

        try {
            if (!playerFile.exists()) {
                playerFile.createNewFile();
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = new JsonObject();
            for (Knowledge knowledge : currentKnowledge) {
                ResourceLocation rl = player.level().registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).getKey(knowledge);
                if (rl != null) {
                    jsonObject.addProperty(rl.toString(), true);
                }
            }

            Files.writeString(playerFile.toPath(), gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Unlocks getKnowledgeUnlock(Level level, Knowledge knowledge) {
        List<Unlocks> unlocksL = level.registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY).stream().toList();
        for (Unlocks u : unlocksL) {
            if (u.knowledgeName().equals(knowledge.knowledgeName()) && u.level() == knowledge.level()) {
                return u;
            }
        }

        if (knowledge != null) {
            CuriosityMod.LOGGER.error("The knowledge " + knowledge.knowledgeName() + " have no unlocks registered");
        } else {
            CuriosityMod.LOGGER.warn("The knowledge is null when trying to get unlock from knowledge");
        }

        return null;
    }

    public static Knowledge getKnowledgeFromUnlock(Level level, Unlocks unlocks) {
        if (unlocks != null) {
            List<Knowledge> knowledgeL = level.registryAccess().registryOrThrow(CuriosityMod.KNOWLEDGE_REGISTRY).stream().toList();
            for (Knowledge k : knowledgeL) {
                if (k.knowledgeName().equals(unlocks.knowledgeName()) && k.level() == unlocks.level()) {
                    return k;
                }
            }
            CuriosityMod.LOGGER.error("The unlock " + unlocks.knowledgeName() + " have no knowledge registered");
        }

        return null;
    }

    public static Unlocks getUnlockFromItem(Level level, ItemStack itemStack) {
        List<Unlocks> unlocksL = level.registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY).stream().toList();
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemRL == null) {
            return null;
        }
        for (Unlocks u : unlocksL) {
            if (u.unlocks().contains(itemRL)) {
                return u;
            }
        }

        return null;
    }

    public static Unlocks getUnlockFromItem(Level level, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
        List<Unlocks> unlocksL = level.registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY).stream().toList();
        for (Unlocks u : unlocksL) {
            for (ResourceLocation rl : u.unlocks()) {
                net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item != null && item.builtInRegistryHolder().is(tag)) {
                    return u;
                }
            }
        }

        return null;
    }

    public static Unlocks getUnlockFromIngredient(Level level, net.minecraft.world.item.crafting.Ingredient ingredient) {
        List<Unlocks> unlocksL = level.registryAccess().registryOrThrow(CuriosityMod.UNLOCK_REGISTRY).stream().toList();

        for (Unlocks u : unlocksL) {
            for (ResourceLocation rl : u.unlocks()) {
                net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item == null) continue;
                if (ingredient.test(new ItemStack(item))) {
                    return u;
                }
            }
        }

        // Em último caso, verifica os ItemStack explícitos do Ingredient
        for (ItemStack itemStack : ingredient.getItems()) {
            ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
            if (itemRL == null) {
                continue;
            }
            for (Unlocks u : unlocksL) {
                if (u.unlocks().contains(itemRL)) {
                    return u;
                }
            }
        }

        return null;
    }
}
