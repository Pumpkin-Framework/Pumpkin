package nl.jk5.pumpkin.server.world.gen.empty;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

@NonnullByDefault
public class VoidGenerationPopulator implements GenerationPopulator {

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomes) {
        if(buffer.containsBlock(world.getSpawnLocation().getBlockPosition())){
            buffer.setBlock(world.getSpawnLocation().getBlockPosition(), BlockState.builder().blockType(BlockTypes.BEDROCK).build());
        }
        if(buffer.containsBlock(0, 63, 0)){
            buffer.setBlock(0, 63, 0, BlockState.builder().blockType(BlockTypes.BEDROCK).build());
        }
    }
}
