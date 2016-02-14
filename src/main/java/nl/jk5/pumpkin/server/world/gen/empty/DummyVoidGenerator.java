package nl.jk5.pumpkin.server.world.gen.empty;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;

@NonnullByDefault
public class DummyVoidGenerator implements GeneratorType {

    @Override
    public String getId() {
        return "void";
    }

    @Override
    public String getName() {
        return "Void";
    }

    @Override
    public DataContainer getGeneratorSettings() {
        return null;
    }

    @Override
    public WorldGenerator createGenerator(World world) {
        return null;
    }
}
