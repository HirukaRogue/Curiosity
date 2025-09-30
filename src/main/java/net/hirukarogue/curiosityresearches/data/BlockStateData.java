package net.hirukarogue.curiosityresearches.data;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class BlockStateData extends BlockStateProvider {
    public BlockStateData(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CuriosityMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ResearchTableRegistry.RESEARCH_TABLE.get(), new ModelFile.UncheckedModelFile("curiosity_researches:block/research_table"));

    }
}