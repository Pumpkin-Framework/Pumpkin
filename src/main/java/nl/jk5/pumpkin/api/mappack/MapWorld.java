package nl.jk5.pumpkin.api.mappack;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface MapWorld {

    MappackWorld getConfig();

    Location<World> getSpawnPoint();

    World getWorld();
}
