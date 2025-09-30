package net.hirukarogue.curiosityresearches.data;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemModelData  extends ItemModelProvider {

    public ItemModelData(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CuriosityMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //research parches
        simpleItem(ResearchItemsRegistry.COMMON_RESEARCH);
        simpleItem(ResearchItemsRegistry.UNCOMMON_RESEARCH);
        simpleItem(ResearchItemsRegistry.RARE_RESEARCH);
        simpleItem(ResearchItemsRegistry.EPIC_RESEARCH);
        simpleItem(ResearchItemsRegistry.LEGENDARY_RESEARCH);
        simpleItem(ResearchItemsRegistry.MYTHIC_RESEARCH);

        //research notes and incomplete research
        simpleItem(ResearchItemsRegistry.RESEARCH_NOTES);
        simpleItem(ResearchItemsRegistry.INCOMPLETE_RESEARCH);

        //Knowledge books
        simpleItem(ResearchItemsRegistry.KNOWLEDGE_BOOK);
        simpleItem(ResearchItemsRegistry.SHARED_RESEARCH_BOOK);

        //ink and quills
        simpleItem(ResearchItemsRegistry.EMPTY_INK_AND_QUILL);
        simpleItem(ResearchItemsRegistry.INK_AND_QUILL);

        //research blocks
        evenSimplerBlockItem(ResearchTableRegistry.RESEARCH_TABLE);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CuriosityMod.MOD_ID,"item/" + item.getId().getPath()));
    }

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        this.withExistingParent(CuriosityMod.MOD_ID + ":" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(CuriosityMod.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CuriosityMod.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CuriosityMod.MOD_ID,"block/" + item.getId().getPath()));
    }
}