package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.api.utils.PlayerLocation;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;

import java.util.Collection;

public interface MappackWorld {
    int getId();

    Mappack getMappack();

    String getName();

    GeneratorType getGenerator();

    DimensionType getDimension();

    boolean isDefault();

    long getSeed();

    PlayerLocation getSpawnpoint();

    GameMode getGamemode();

    boolean shouldGenerateStructures();

    int getInitialTime();

    String getGeneratorOptions();

    @SuppressWarnings("unchecked")
    Collection<WorldFile> getFiles();

    @SuppressWarnings("unchecked")
    Collection<Zone> getZones();
}
