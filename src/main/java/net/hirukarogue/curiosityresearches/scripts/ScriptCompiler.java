package net.hirukarogue.curiosityresearches.scripts;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Unlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ScriptCompiler {
    private List<ScriptBasicEncode> knowledgeCompilation;

    private ScriptCompiler(List<ScriptBasicEncode> knowledgeCompilation) {
        this.knowledgeCompilation = knowledgeCompilation;
    }

    private Codec<ScriptCompiler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ScriptBasicEncode.CODEC).fieldOf("knowledge_compilation").forGetter(ScriptCompiler::getKnowledgeCompilation)
    ).apply(instance, ScriptCompiler::new));

    private List<ScriptBasicEncode> getKnowledgeCompilation() {
        return knowledgeCompilation;
    }

    public void compile() {
        File knowledgeScriptDir = Minecraft.getInstance().gameDirectory.toPath().resolve("knowledge_scripts").toFile();
        if (!knowledgeScriptDir.exists()) knowledgeScriptDir.mkdirs();
        File[] scriptFiles = knowledgeScriptDir.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".txt") || name.endsWith(".kjs"));
        if (scriptFiles == null) return;
        for (File scriptFile : scriptFiles) {
            try {
                JsonObject json = GsonHelper.parse(Files.newBufferedReader(scriptFile.toPath()));
                ScriptCompiler compiler = CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).result().orElse(null);
                if (compiler != null) {
                    for (ScriptBasicEncode encode : compiler.getKnowledgeCompilation()) {
                        Knowledge knowledge = new Knowledge(encode.getKnowledgeName(), encode.getKnowledgeDescription());
                        for (ScriptElement element : encode.getUnlockedStuffs()) {
                            Object obj = element.getElement();
                            if (obj instanceof BlockItem) {
                                knowledge.getUnlocks().getUnlocked_blocks().add(((BlockItem) obj).getBlock());
                                knowledge.getUnlocks().getUnlocked_items().add(((BlockItem) obj));
                                knowledge.getUnlocks().getUnlocked_recipes().add()
                            } else if (obj instanceof Knowledge) {
                                knowledge.getUnlocks().getUnlocked_knowledge().add((Knowledge) obj);
                            } else if (obj instanceof net.minecraft.world.item.Item) {
                                knowledge.getUnlocks().getUnlocked_items().add((net.minecraft.world.item.Item) obj);
                            } else if (obj instanceof TagKey) {
                                @SuppressWarnings("unchecked")
                                TagKey<Item> tag = (TagKey<Item>) obj;
                                knowledge.getUnlocks().getUnlocked_tags().add(tag);
                            } else {
                                CuriosityMod.LOGGER.error("Unknown element type in script file " + scriptFile.getName() + ": " + obj.getClass().getName());
                            }
                        }
                    }
                } else {
                    CuriosityMod.LOGGER.error("Failed to parse script file " + scriptFile.getName());
                }

            } catch (Exception e) {
                CuriosityMod.LOGGER.error("Failed to compile script file " + scriptFile.getName() + ": " + e.getMessage());
            }
        }
    }
}
