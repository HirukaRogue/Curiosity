package net.hirukarogue.curiosityresearches.scripts;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeData;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.miscellaneous.researchcomponent.Component;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.KnowledgeBook;
import net.hirukarogue.curiosityresearches.researchparches.researchitems.ResearchParchment;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class ScriptCompiler {
    private final List<Object> knowledgeCompilation;

    private ScriptCompiler(List<Object> knowledgeCompilation) {
        this.knowledgeCompilation = knowledgeCompilation;
    }

    private static final Codec<Object> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            KnowledgeEncode.CODEC.optionalFieldOf("knowledge_encode").forGetter(ke -> ke instanceof KnowledgeEncode ? Optional.of((KnowledgeEncode) ke) : Optional.empty()),
            KBookEncode.CODEC.optionalFieldOf("knowledge_book").forGetter(kb -> kb instanceof KBookEncode ? Optional.of((KBookEncode) kb) : Optional.empty()),
            ParchmentEncode.CODEC.optionalFieldOf("research_parchment").forGetter(pe -> pe instanceof ParchmentEncode ? Optional.of((ParchmentEncode) pe) : Optional.empty())
    ).apply(instance, (knowledgeOpt, kbookOpt, parchmentOpt) -> {
        if (knowledgeOpt != null && knowledgeOpt.isPresent()) {
            return knowledgeOpt.get();
        } else if (kbookOpt != null && kbookOpt.isPresent()) {
            return kbookOpt.get();
        } else if (parchmentOpt != null && parchmentOpt.isPresent()) {
            return parchmentOpt.get();
        } else {
            throw new IllegalArgumentException("One of 'knowledge_book' or 'parchment' must be provided.");
        }
    }));

    private List<Object> getKnowledgeCompilation() {
        return knowledgeCompilation;
    }

    public static void compile() {
        String recipeDirPath = "data/" + CuriosityMod.MOD_ID + "/recipes/";
        File recipeDir = new File(recipeDirPath);

        try {
            if (!recipeDir.exists()) {
                recipeDir.mkdirs();
            }
            File[] existingFiles = recipeDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (existingFiles != null) {
                for (File file : existingFiles) {
                    try {
                        Files.deleteIfExists(file.toPath());
                        CuriosityMod.LOGGER.info("Deleted recipe file: {}", file.getName());
                    } catch (Exception e) {
                        CuriosityMod.LOGGER.error("Failed to delete recipe file {}: {}", file.getName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            CuriosityMod.LOGGER.error("Failed to clean recipes directory: {}", e.getMessage());
        }

        File knowledgeScriptDir = Minecraft.getInstance().gameDirectory.toPath().resolve("knowledge_scripts").toFile();
        if (!knowledgeScriptDir.exists()) knowledgeScriptDir.mkdirs();
        File[] scriptFiles = knowledgeScriptDir.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".kjs"));
        if (scriptFiles == null) return;
        Map<String,Knowledge> relatedKnowledges = new HashMap<>();
        for (File scriptFile : scriptFiles) {
            try {
                JsonObject json = GsonHelper.parse(Files.newBufferedReader(scriptFile.toPath()));
                ScriptCompiler compiler = (ScriptCompiler) CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).result().orElse(null);
                if (compiler != null) {
                    Map<String, Component> globalComponents = new HashMap<>();
                    for (Object encode : compiler.getKnowledgeCompilation()) {
                        if (encode instanceof KnowledgeEncode knowledgeEncode) {
                            Knowledge knowledge = processKnowledge(knowledgeEncode);

                            KnowledgeData data = new KnowledgeData(null);
                            data.registerInEncyclopedia(knowledge);
                            relatedKnowledges.put(knowledgeEncode.key(), knowledge);
                        } else if (encode instanceof ParchmentEncode parchmentEncode) {
                            if (!relatedKnowledges.containsKey(parchmentEncode.knowledgeKey())) {
                                throw new RuntimeException("The knowledge key '" + parchmentEncode.knowledgeKey() + "' is not registered.");
                            }
                            Knowledge knowledge = relatedKnowledges.get(parchmentEncode.knowledgeKey());

                            Item research_parch = switch (parchmentEncode.tier()) {
                                case "common" -> ResearchItemsRegistry.COMMON_RESEARCH.get();
                                case "uncommon" -> ResearchItemsRegistry.UNCOMMON_RESEARCH.get();
                                case "rare" -> ResearchItemsRegistry.RARE_RESEARCH.get();
                                case "epic" -> ResearchItemsRegistry.EPIC_RESEARCH.get();
                                case "legendary" -> ResearchItemsRegistry.LEGENDARY_RESEARCH.get();
                                case "MYTHIC" -> ResearchItemsRegistry.MYTHIC_RESEARCH.get();
                                default -> throw new RuntimeException("Invalid knowledge_tier: " + parchmentEncode.tier()
                                        + "\nknowledge_tier must be one of common, uncommon, rare, epic, legendary or MYTHIC"
                                );
                            };

                            ((ResearchParchment) research_parch).setKnowledge(knowledge);
                            ((ResearchParchment) research_parch).setCustomName(parchmentEncode.customName() != null ? parchmentEncode.customName() : (knowledge.getKnowledge_name() + " Research Parchment"));

                            if (parchmentEncode.pattern().size() != 3) {
                                throw new RuntimeException("The pattern don't fit");
                            }

                            if (parchmentEncode.pattern().get(0).length() != 2) {
                                throw new RuntimeException("The first line should always be 2 elements, including space for empty slots");
                            }
                            if (parchmentEncode.pattern().get(1).length() != 3) {
                                throw new RuntimeException("The second line should always be 3 elements, including space for empty slots");
                            }
                            if (parchmentEncode.pattern().get(2).length() != 2) {
                                throw new RuntimeException("The third line should always be 2 elements, including space for empty slots");
                            }

                            for (String key : globalComponents.keySet()) {
                                if (parchmentEncode.keys().containsKey(key)) {
                                    throw new RuntimeException("Key conflict: the key '" + key + "' is already defined in global components.");
                                }
                            }

                            boolean isValid = true;
                            Map<String, Component> keysToJoin = new HashMap<>();

                            for (String line : parchmentEncode.pattern()) {
                                String[] characters = line.split("");
                                for (String character : characters) {
                                    if (Objects.equals(character, " ")) {
                                        continue;
                                    }

                                    if (!parchmentEncode.keys().containsKey(character) || !globalComponents.containsKey(character)) {
                                        CuriosityMod.LOGGER.error("You didn't registered this key");
                                        isValid = false;
                                        break;
                                    }

                                    if (globalComponents.containsKey(character)) {
                                        keysToJoin.put(character, globalComponents.get(character));
                                    }
                                }
                                if (!isValid) {
                                    break;
                                }
                            }

                            if (!keysToJoin.isEmpty()) {
                                for (String key : keysToJoin.keySet()) {
                                    parchmentEncode.keys().put(key, keysToJoin.get(key));
                                }
                            }

                            if (!isValid) {
                                throw new RuntimeException("Invalid pattern: contains unregistered keys");
                            }

                            com.google.gson.JsonObject new_recipe = new com.google.gson.JsonObject();

                            new_recipe.add("keys", Codec.unboundedMap(Codec.STRING, Component.CODEC).encodeStart(com.mojang.serialization.JsonOps.INSTANCE, parchmentEncode.keys()).result().orElseThrow(() -> new RuntimeException("Failed to encode keys")));
                            new_recipe.add("pattern", com.mojang.serialization.Codec.list(Codec.STRING).encodeStart(com.mojang.serialization.JsonOps.INSTANCE, parchmentEncode.pattern()).result().orElseThrow(() -> new RuntimeException("Failed to encode pattern")));

                            new_recipe.add("knowledge", Knowledge.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, knowledge).result().orElseThrow(() -> new RuntimeException("Failed to encode knowledge")));

                            new_recipe.add("custom_name", Codec.STRING.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, parchmentEncode.customName()).result().orElseThrow(() -> new RuntimeException("Failed to encode custom name")));

                            new_recipe.add("research_result", ForgeRegistries.ITEMS.getCodec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, research_parch).result().orElseThrow(() -> new RuntimeException("Failed to encode research result")));

                            if (parchmentEncode.requiredPaper() < 1 || parchmentEncode.requiredPaper() > 64) {
                                throw new RuntimeException("required_paper must be between 1 and 64");
                            }

                            new_recipe.add("required_paper", Codec.INT.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, parchmentEncode.requiredPaper()).result().orElseThrow(() -> new RuntimeException("Failed to encode required paper")));

                            File recipeFile = new File("data/" + CuriosityMod.MOD_ID + "/recipes/" + knowledge.getKnowledge_name().toLowerCase().replace(" ", "_") + "_research.json");

                            if (recipeFile == null) {
                                recipeFile.mkdirs();
                            }

                            Files.writeString(recipeFile.toPath(), com.mojang.serialization.JsonOps.INSTANCE.convertTo(com.mojang.serialization.JsonOps.INSTANCE, new_recipe).toString());

                            KnowledgeData data = new KnowledgeData(null);
                            data.registerInEncyclopedia(knowledge);
                        } else if (encode instanceof KBookEncode kbookEncode) {
                            Item knowledge_book = ResearchItemsRegistry.KNOWLEDGE_BOOK.get();
                            ((KnowledgeBook) knowledge_book).setCustomName(
                                    kbookEncode.customName() != null ?
                                            kbookEncode.customName() :
                                            "Knowledge Book"
                            );
                            List<Knowledge> knowledges = new ArrayList<>();
                            for (String knowledgeRelation : kbookEncode.associatedKnowledgeKeys()) {
                                if (!relatedKnowledges.containsKey(knowledgeRelation)) {
                                    throw new RuntimeException("The knowledge key '" + knowledgeRelation + "' is not registered.");
                                }
                                Knowledge knowledge = relatedKnowledges.get(knowledgeRelation);
                                knowledges.add(knowledge);
                                KnowledgeData data = new KnowledgeData(null);
                                data.registerInEncyclopedia(knowledge);
                            }
                            ((KnowledgeBook) knowledge_book).setBookKnowledges(knowledges);
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

    private static Knowledge processKnowledge(KnowledgeEncode knowledgeSubEncode) {
        Knowledge knowledge = new Knowledge(knowledgeSubEncode.knowledgeName(), knowledgeSubEncode.knowledgeDescription());
        for (ScriptElement element : knowledgeSubEncode.thingsToUnlock()) {
            Object obj = element.element();
            if (obj instanceof BlockItem item) {
                if (Objects.equals(element.config(), "item_only")) {
                    knowledge.getUnlocks().getUnlocked_items().add(item);
                    continue;
                }
                if (Objects.equals(element.config(), "block_only")) {
                    knowledge.getUnlocks().getUnlocked_blocks().add(item.getBlock());
                    continue;
                }
                if (Objects.equals(element.config(), "thing_only")) {
                    knowledge.getUnlocks().getUnlocked_items().add(item);
                    knowledge.getUnlocks().getUnlocked_blocks().add(item.getBlock());
                    continue;
                }
                if (Objects.equals(element.config(), "recipe_only")) {
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
                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item))
                                    return true;
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
                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item))
                                return true;
                        } catch (Throwable ignored) {
                        }

                        return false;
                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                }
            } else if (obj instanceof Knowledge unlocked) {
                knowledge.getUnlocks().getUnlocked_knowledge().add(unlocked);
            } else if (obj instanceof Item item) {
                if (Objects.equals(element.config(), "item_only") || Objects.equals(element.config(), "thing_only")) {
                    knowledge.getUnlocks().getUnlocked_items().add(item);
                    continue;
                }

                if (Objects.equals(element.config(), "recipe_only")) {
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
                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item))
                                    return true;
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
                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(item))
                                return true;
                        } catch (Throwable ignored) {
                        }

                        return false;
                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                }
            } else if (obj instanceof TagKey) {
                @SuppressWarnings("unchecked")
                TagKey<Item> tag = (TagKey<Item>) obj;
                if (Objects.equals(element.config(), "tag_only")) {
                    knowledge.getUnlocks().getUnlocked_tags().add(tag);
                    continue;
                }
                if (Objects.equals(element.config(), "recipe_only")) {
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
                                if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(tag))
                                    return true;
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
                            if (r.getResultItem(Minecraft.getInstance().level.registryAccess()).is(tag))
                                return true;
                        } catch (Throwable ignored) {
                        }

                        return false;
                    }).forEach(r -> knowledge.getUnlocks().getUnlocked_recipes().add(r.getId().toString()));
                }
            } else {
                CuriosityMod.LOGGER.error("Unknown element : " + obj.getClass().getName());
            }
        }
        return knowledge;
    }
}
