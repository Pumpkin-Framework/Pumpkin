package nl.jk5.pumpkin.server.world.gen.empty;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

@NonnullByDefault
public class VoidWorldGeneratorModifier implements WorldGeneratorModifier {

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator worldGenerator) {
        worldGenerator.setBaseGenerationPopulator(new VoidGenerationPopulator());
        worldGenerator.setBiomeGenerator(new VoidBiomeGenerator());
        worldGenerator.getGenerationPopulators().clear();
        worldGenerator.getPopulators().clear();
    }

    @Override
    public String getId() {
        return "pumpkin:void";
    }

    @Override
    public String getName() {
        return "Void";
    }
}
