package net.hirukarogue.curiosityresearches.scripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.hirukarogue.curiosityresearches.miscellaneous.knowledge.Knowledge;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public record ScriptElement (
        Object element,
        String config
) {
    /*private Object element;
    private String config;*/

    /*private ScriptElement(Object element, String config) {
        this.element = element;
        this.config = config;
    }*/

    public static final Codec<ScriptElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ITEMS.getCodec().optionalFieldOf("item").forGetter(se -> se.element instanceof Item && !(se.element instanceof BlockItem) ? Optional.of((Item) se.element) : Optional.empty()),
            TagKey.codec(ForgeRegistries.ITEMS.getRegistryKey()).optionalFieldOf("tag").forGetter(se -> se.element instanceof TagKey ? Optional.of((TagKey<Item>) se.element) : Optional.empty()),
            Knowledge.CODEC.optionalFieldOf("knowledge").forGetter(se -> se.element instanceof Knowledge ? Optional.of((Knowledge) se.element) : Optional.empty()),
            ForgeRegistries.ITEMS.getCodec().optionalFieldOf("block_item").forGetter(se -> se.element instanceof BlockItem ? Optional.of((Item) se.element) : Optional.empty()),
            Codec.STRING.optionalFieldOf("config", "").forGetter(ScriptElement::config)
    ).apply(instance, (itemOpt, tagOpt, knowledgeOpt, blockOpt, config) -> {
        Object element = null;
        if (blockOpt != null && blockOpt.isPresent()) {
            Item it = blockOpt.get();
            if (it instanceof BlockItem) element = it;
            else element = it;
        }
        else if (tagOpt != null && tagOpt.isPresent()) element = tagOpt.get();
        else if (knowledgeOpt != null && knowledgeOpt.isPresent()) element = knowledgeOpt.get();
        else if (itemOpt != null && itemOpt.isPresent()) element = itemOpt.get();
        else throw new IllegalArgumentException("One of 'item', 'tag', 'knowledge', or 'block_item' must be provided.");
        return new ScriptElement(element, config);
    }));

    /*public Object getElement() {
        return element;
    }

    public String getConfig() {
        return config;
    }*/
}
