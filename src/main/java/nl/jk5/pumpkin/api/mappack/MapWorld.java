package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.server.map.stat.StatEmitter;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public interface MapWorld {

    MappackWorld getConfig();

    Location<World> getSpawnPoint();

    World getWorld();

    Map getMap();

    Collection<StatEmitter> getStatEmitters(); //TODO: remove core class StatEmitter from api
}
