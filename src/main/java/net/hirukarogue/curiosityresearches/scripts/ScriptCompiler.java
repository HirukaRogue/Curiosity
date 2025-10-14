package net.hirukarogue.curiosityresearches.scripts;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class ScriptCompiler {
    private final List<ScriptBasicEncode> knowledgeCompilation;

    private ScriptCompiler(List<ScriptBasicEncode> knowledgeCompilation) {
        this.knowledgeCompilation = knowledgeCompilation;
    }

    private final Codec<ScriptCompiler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
                        for (ScriptElement element : encode.getThingsToUnlock()) {
                            Object obj = element.getElement();
                            if (obj instanceof BlockItem item) {
                                if (Objects.equals(element.getConfig(), "item_only")) {
                                    knowledge.getUnlocks().getUnlocked_items().add(item);
                                    continue;
                                }
                                if (Objects.equals(element.getConfig(), "block_only")) {
                                    knowledge.getUnlocks().getUnlocked_blocks().add(item.getBlock());
                                    continue;
                                }
                                if (Objects.equals(element.getConfig(), "thing_only")) {
                                    knowledge.getUnlocks().getUnlocked_items().add(item);
                                    knowledge.getUnlocks().getUnlocked_blocks().add(item.getBlock());
                                    continue;
                                }
                                if (Objects.equals(element.getConfig(), "recipe_only")) {
                                    if (Minecraft.getInstance().level != null) {
                                        net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                        rm.getRecipes().stream().filter(r -> {
                                            // Try to inspect recipe ingredients generically
                                            try {
                                                List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                                if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(item))) {
                                                    return true;
                                                }
                                            } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                                // Some recipe implementations might not expose getIngredients; fall back below
                                            } catch (Throwable ignored) {
                                            }

                                            // Fallback: check result item when available
                                            try {
                                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item)) return true;
                                            } catch (Throwable ignored) {
                                            }

                                            return false;
                                        }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                    }
                                    continue;
                                }

                                knowledge.getUnlocks().getUnlocked_items().add(item);
                                knowledge.getUnlocks().getUnlocked_blocks().add(item.getBlock());
                                if (Minecraft.getInstance().level != null) {
                                    net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                    rm.getRecipes().stream().filter(r -> {
                                        // Try to inspect recipe ingredients generically
                                        try {
                                            List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                            if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(item))) {
                                                return true;
                                            }
                                        } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                            // Some recipe implementations might not expose getIngredients; fall back below
                                        } catch (Throwable ignored) {
                                        }

                                        // Fallback: check result item when available
                                        try {
                                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item)) return true;
                                        } catch (Throwable ignored) {
                                        }

                                        return false;
                                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                }
                            } else if (obj instanceof Knowledge unlocked) {
                                knowledge.getUnlocks().getUnlocked_knowledge().add(unlocked);
                            } else if (obj instanceof Item item) {
                                if (Objects.equals(element.getConfig(), "item_only") || Objects.equals(element.getConfig(), "thing_only")) {
                                    knowledge.getUnlocks().getUnlocked_items().add(item);
                                    continue;
                                }

                                if (Objects.equals(element.getConfig(), "recipe_only")) {
                                    if (Minecraft.getInstance().level != null) {
                                        net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                        rm.getRecipes().stream().filter(r -> {
                                            // Try to inspect recipe ingredients generically
                                            try {
                                                List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                                if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(item))) {
                                                    return true;
                                                }
                                            } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                                // Some recipe implementations might not expose getIngredients; fall back below
                                            } catch (Throwable ignored) {
                                            }

                                            // Fallback: check result item when available
                                            try {
                                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item)) return true;
                                            } catch (Throwable ignored) {
                                            }

                                            return false;
                                        }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                    }
                                    continue;
                                }

                                knowledge.getUnlocks().getUnlocked_items().add(item);
                                if (Minecraft.getInstance().level != null) {
                                    net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                    rm.getRecipes().stream().filter(r -> {
                                        // Try to inspect recipe ingredients generically
                                        try {
                                            List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                            if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(item))) {
                                                return true;
                                            }
                                        } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                            // Some recipe implementations might not expose getIngredients; fall back below
                                        } catch (Throwable ignored) {
                                        }

                                        // Fallback: check result item when available
                                        try {
                                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item)) return true;
                                        } catch (Throwable ignored) {
                                        }

                                        return false;
                                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                }
                            } else if (obj instanceof TagKey) {
                                @SuppressWarnings("unchecked")
                                TagKey<Item> tag = (TagKey<Item>) obj;
                                if (Objects.equals(element.getConfig(), "tag_only")) {
                                    knowledge.getUnlocks().getUnlocked_tags().add(tag);
                                    continue;
                                }
                                if (Objects.equals(element.getConfig(), "recipe_only")) {
                                    if (Minecraft.getInstance().level != null) {
                                        net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                        rm.getRecipes().stream().filter(r -> {
                                            // Try to inspect recipe ingredients generically
                                            try {
                                                List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                                if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(tag))) {
                                                    return true;
                                                }
                                            } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                                // Some recipe implementations might not expose getIngredients; fall back below
                                            } catch (Throwable ignored) {
                                            }

                                            // Fallback: check result item when available
                                            try {
                                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(tag)) return true;
                                            } catch (Throwable ignored) {
                                            }

                                            return false;
                                        }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                        continue;
                                    }
                                }
                                knowledge.getUnlocks().getUnlocked_tags().add(tag);
                                if (Minecraft.getInstance().level != null) {
                                    net.minecraft.world.item.crafting.RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
                                    rm.getRecipes().stream().filter(r -> {
                                        // Try to inspect recipe ingredients generically
                                        try {
                                            List<net.minecraft.world.item.crafting.Ingredient> ingredients = r.getIngredients();
                                            if (ingredients.stream().anyMatch(ing -> ing.getItems().length > 0 && ing.getItems()[0].is(tag))) {
                                                return true;
                                            }
                                        } catch (NoSuchMethodError | AbstractMethodError ignored) {
                                            // Some recipe implementations might not expose getIngredients; fall back below
                                        } catch (Throwable ignored) {
                                        }

                                        // Fallback: check result item when available
                                        try {
                                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(tag)) return true;
                                        } catch (Throwable ignored) {
                                        }

                                        return false;
                                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                                }
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
