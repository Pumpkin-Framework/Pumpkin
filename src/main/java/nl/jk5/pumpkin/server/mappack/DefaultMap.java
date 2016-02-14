package nl.jk5.pumpkin.server.mappack;

import com.google.common.base.Objects;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Pumpkin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultMap implements nl.jk5.pumpkin.api.mappack.Map {

    private final Mappack mappack;
    private final Pumpkin pumpkin;
    private final List<DefaultMapWorld> worlds = new ArrayList<>();

    public DefaultMap(Mappack mappack, Pumpkin pumpkin){
        this.mappack = mappack;
        this.pumpkin = pumpkin;
    }

    public Mappack getMappack() {
        return mappack;
    }

    public void addWorld(DefaultMapWorld world) {
        this.worlds.add(world);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<MapWorld> getWorlds() {
        return ((Collection) worlds);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mappack", mappack)
                .add("worlds", worlds)
                .toString();
    }
}
