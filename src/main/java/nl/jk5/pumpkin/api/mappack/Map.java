package nl.jk5.pumpkin.api.mappack;

import java.util.Collection;

public interface Map {

    Collection<MapWorld> getWorlds();

    MapWorld getDefaultWorld();
}
