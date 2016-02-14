package nl.jk5.pumpkin.server.world.gen.empty;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;

@NonnullByDefault
public class VoidBiomeGenerator implements BiomeGenerator {

    @Override
    @SuppressWarnings("ConstantConditions")
    public void generateBiomes(MutableBiomeArea buffer) {
        final Vector2i min = buffer.getBiomeMin();
        final Vector2i max = buffer.getBiomeMax();
        for (int yy = min.getY(); yy <= max.getY(); yy++) {
            for (int xx = min.getX(); xx <= max.getX(); xx++) {
                buffer.setBiome(xx, yy, BiomeTypes.PLAINS);
            }
        }
    }
}
