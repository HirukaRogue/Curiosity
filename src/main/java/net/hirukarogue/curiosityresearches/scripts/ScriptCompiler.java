package net.hirukarogue.curiosityresearches.scripts;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.ResearchParchment;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

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
        File[] scriptFiles = knowledgeScriptDir.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".kjs"));
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

                        Item research_parch = switch (encode.getKnowledgeTier()) {
                            case "common" -> ResearchItemsRegistry.COMMON_RESEARCH.get();
                            case "uncommon" -> ResearchItemsRegistry.UNCOMMON_RESEARCH.get();
                            case "rare" -> ResearchItemsRegistry.RARE_RESEARCH.get();
                            case "epic" -> ResearchItemsRegistry.EPIC_RESEARCH.get();
                            case "legendary" -> ResearchItemsRegistry.LEGENDARY_RESEARCH.get();
                            case "MYTHIC" -> ResearchItemsRegistry.MYTHIC_RESEARCH.get();
                            default -> throw new RuntimeException("Invalid knowledge_tier: " + encode.getKnowledgeTier()
                                    + "\nknowledge_tier must be one of common, uncommon, rare, epic, legendary or MYTHIC"
                            );
                        };

                        ((ResearchParchment) research_parch).setKnowledge(knowledge);
                        ((ResearchParchment) research_parch).setCustomName(encode.getCustomName() != null ? encode.getCustomName() : (encode.getKnowledgeName() + " Research Parchment"));

                        if (encode.getPattern().size() != 3) {
                            throw new RuntimeException("The pattern don't fit");
                        }

                        if (encode.getPattern().get(0).length() != 2) {
                            throw new RuntimeException("The first line should always be 2 elements, including space for empty slots");
                        }
                        if (encode.getPattern().get(1).length() != 3) {
                            throw new RuntimeException("The second line should always be 3 elements, including space for empty slots");
                        }
                        if (encode.getPattern().get(2).length() != 2) {
                            throw new RuntimeException("The third line should always be 2 elements, including space for empty slots");
                        }

                        boolean isValid = true;

                        for (String line : encode.getPattern()) {
                            String[] characters = line.split("");
                            for (String character : characters) {
                                if (Objects.equals(character, " ")) {
                                    continue;
                                }

                                if (!encode.getKeys().containsKey(character)) {
                                    CuriosityMod.LOGGER.error("You didn't registered this key");
                                    isValid = false;
                                    break;
                                }
                            }
                            if (!isValid) {
                                break;
                            }
                        }

                        if (!isValid) {
                            throw new RuntimeException("Invalid pattern: contains unregistered keys");
                        }

                        com.google.gson.JsonObject new_recipe = new com.google.gson.JsonObject();

                        new_recipe.add("keys", Codec.unboundedMap(Codec.STRING, Component.CODEC).encodeStart(com.mojang.serialization.JsonOps.INSTANCE, encode.getKeys()).result().orElseThrow(() -> new RuntimeException("Failed to encode keys")));
                        new_recipe.add("pattern", com.mojang.serialization.Codec.list(Codec.STRING).encodeStart(com.mojang.serialization.JsonOps.INSTANCE, encode.getPattern()).result().orElseThrow(() -> new RuntimeException("Failed to encode pattern")));

                        new_recipe.add("knowledge", Knowledge.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, knowledge).result().orElseThrow(() -> new RuntimeException("Failed to encode knowledge")));

                        new_recipe.add("custom_name", Codec.STRING.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, encode.getCustomName()).result().orElseThrow(() -> new RuntimeException("Failed to encode custom name")));

                        new_recipe.add("research_result", ForgeRegistries.ITEMS.getCodec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, research_parch).result().orElseThrow(() -> new RuntimeException("Failed to encode research result")));

                        if (encode.getRequiredPaper() < 1 || encode.getRequiredPaper() > 64) {
                            throw new RuntimeException("required_paper must be between 1 and 64");
                        }

                        new_recipe.add("required_paper", Codec.INT.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, encode.getRequiredPaper()).result().orElseThrow(() -> new RuntimeException("Failed to encode required paper")));

                        File recipeFile = new File("data/" + CuriosityMod.MOD_ID + "/recipes/" + knowledge.getKnowledge_name().toLowerCase().replace(" ", "_") + "_research.json");

                        if (recipeFile == null) {
                            recipeFile.mkdirs();
                        }

                        Files.writeString(recipeFile.toPath(), com.mojang.serialization.JsonOps.INSTANCE.convertTo(com.mojang.serialization.JsonOps.INSTANCE, new_recipe).toString());

                        KnowledgeData data = new KnowledgeData(null);
                        data.registerInEncyclopedia(knowledge);
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
