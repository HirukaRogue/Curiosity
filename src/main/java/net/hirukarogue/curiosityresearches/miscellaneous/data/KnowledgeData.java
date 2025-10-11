package net.hirukarogue.curiosityresearches.miscellaneous.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class KnowledgeData {
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).setPrettyPrinting().create();
    @Nullable
    private final Path knowledgeDir;
    private static final Path encyclopediaDir = Minecraft.getInstance().gameDirectory.toPath().resolve("encyclopedia");

    public KnowledgeData(@Nullable Level level) {
        if (level != null) {
            if (level instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();
                Path worldRoot = server.getWorldPath(LevelResource.ROOT);

                this.knowledgeDir = worldRoot.resolve("curiosityresearches").resolve("playerdata");
            } else {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getSingleplayerServer() != null) {
                    Path worldRoot = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);

                    this.knowledgeDir = worldRoot.resolve("curiosityresearches").resolve("playerdata");
                } else {
                    throw new RuntimeException("PlayerKnowledgeData can only be initialized in a server or singleplayer context.");
                }
            }
            try {
                Files.createDirectories(knowledgeDir);
            } catch (IOException e) {
                CuriosityMod.LOGGER.error("Failed to create knowledge directory: " + e.getMessage());
            }
        } else {
            this.knowledgeDir = null;
            CuriosityMod.LOGGER.error("PlayerKnowledgeData initialized with null level.");
        }
    }

    public void addKnowledgeToPlayer(Player player, Knowledge knowledge) {
        List<Knowledge> currentKnowledges = loadPlayerKnowledge(player);
        if (currentKnowledges.stream().noneMatch(k -> k.getKnowledge_name().equals(knowledge.getKnowledge_name()))) {
            currentKnowledges = new ArrayList<>(currentKnowledges);
            currentKnowledges.add(knowledge);
            savePlayerKnowledge(player, currentKnowledges);
        }
    }

    public void savePlayerKnowledge(Player player, List<Knowledge> knowledges) {
        File playerKnowledgeJson = new File(knowledgeDir + File.separator + player.getUUID() + "_knowledge.json");

        try {
            if (!playerKnowledgeJson.getParentFile().exists()) {
                if (!playerKnowledgeJson.getParentFile().mkdirs()) {
                    CuriosityMod.LOGGER.error("Failed to create directories for player knowledge data.");
                    return;
                }
            }

            if (!playerKnowledgeJson.exists()) {
                if (!playerKnowledgeJson.createNewFile()) {
                    CuriosityMod.LOGGER.error("Failed to create player knowledge data file.");
                    return;
                }
            }

            com.google.gson.JsonObject knowledgeCompilation = new com.google.gson.JsonObject();
            knowledgeCompilation.add("knowledges", Knowledge.CODEC.listOf().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, knowledges).result().orElseThrow(() -> new RuntimeException("Failed to encode knowledges")));

            try (java.io.FileWriter writer = new java.io.FileWriter(playerKnowledgeJson)) {
                GSON.toJson(knowledgeCompilation, writer);
            }
        } catch (Exception e) {
            CuriosityMod.LOGGER.error("Error saving player knowledge data: " + e.getMessage());
        }
    }

    public List<Knowledge> loadPlayerKnowledge(Player player) {
        File playerKnowledgePath = new File(knowledgeDir + File.separator + player.getUUID() + "_knowledge.json");
        try {
            if (playerKnowledgePath.exists()) {
                try (JsonReader jsonReader = javax.json.Json.createReader(new java.io.FileReader(playerKnowledgePath))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    com.google.gson.JsonObject knowledgeCompilation = GsonHelper.getAsJsonObject(GSON.toJsonTree(jsonObject).getAsJsonObject(), "knowledges");

                    return Knowledge.CODEC.listOf().parse(com.mojang.serialization.JsonOps.INSTANCE, knowledgeCompilation).result().orElse(List.of());
                }
            } else {
                return List.of();
            }
        } catch (Exception e) {
            CuriosityMod.LOGGER.error("Error loading player knowledge data: " + e.getMessage());
            return List.of();
        }
    }

    public void registerInEncyclopedia(Knowledge knowledge) {
        List<Knowledge> allKnowledges = loadCompleteEncyclopedia();
        if (allKnowledges.stream().noneMatch(k -> k.getKnowledge_name().equals(knowledge.getKnowledge_name()))) {
            allKnowledges = new ArrayList<>(allKnowledges);
            allKnowledges.add(knowledge);

            File all_knowledge_registered = new File(encyclopediaDir + File.separator +"/all_knowledge.json");
            try {
                if (!all_knowledge_registered.getParentFile().exists()) {
                    if (!all_knowledge_registered.getParentFile().mkdirs()) {
                        CuriosityMod.LOGGER.error("Failed to create directories for encyclopedia data.");
                        return;
                    }
                }

                if (!all_knowledge_registered.exists()) {
                    if (!all_knowledge_registered.createNewFile()) {
                        CuriosityMod.LOGGER.error("Failed to create encyclopedia data file.");
                        return;
                    }
                }

                com.google.gson.JsonObject knowledgeCompilation = new com.google.gson.JsonObject();
                knowledgeCompilation.add("knowledges", Knowledge.CODEC.listOf().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, allKnowledges).result().orElseThrow(() -> new RuntimeException("Failed to encode knowledges")));

                try (java.io.FileWriter writer = new java.io.FileWriter(all_knowledge_registered)) {
                    GSON.toJson(knowledgeCompilation, writer);
                }
            } catch (IOException e) {
                CuriosityMod.LOGGER.error("Error saving encyclopedia data: " + e.getMessage());
            }
        }
    }

    public List<Knowledge> loadCompleteEncyclopedia() {
        File all_knowledge_registered = new File(encyclopediaDir + File.separator +"/all_knowledge.json");
        try {
            if (all_knowledge_registered.exists()) {
                String jsonContent = Files.readString(all_knowledge_registered.toPath());
                Knowledge[] knowledgeArray = GSON.fromJson(jsonContent, Knowledge[].class);
                return List.of(knowledgeArray);
            } else {
                CuriosityMod.LOGGER.warn("Encyclopedia file not found: " + all_knowledge_registered.getAbsolutePath());
            }
        } catch (IOException e) {
            CuriosityMod.LOGGER.error("Failed to read encyclopedia file: " + e.getMessage());
        }
        return List.of();
    }
}
