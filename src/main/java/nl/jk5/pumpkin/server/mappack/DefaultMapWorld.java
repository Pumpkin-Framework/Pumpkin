package nl.jk5.pumpkin.server.mappack;

import com.google.common.base.Objects;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.MappackWorld;
import nl.jk5.pumpkin.server.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class DefaultMapWorld implements MapWorld {

    private final World world;
    private final MappackWorld mappackWorld;

    public DefaultMapWorld(World world, MappackWorld mappackWorld) {
        this.world = world;
        this.mappackWorld = mappackWorld;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("world", world)
                .toString();
    }

    public void destroy(){
        Log.info("Destroying world " + world.getName());
        Sponge.getGame().getServer().unloadWorld(world);
        Sponge.getGame().getServer().deleteWorld(world.getProperties());
    }

    public World getWorld() {
        return world;
    }

    @Override
    public Location<World> getSpawnPoint() {
        return new Location<>(this.getWorld(), this.getConfig().getSpawnpoint().getX(), this.getConfig().getSpawnpoint().getY(), this.getConfig().getSpawnpoint().getZ());
    }

    @Override
    public MappackWorld getConfig() {
        return mappackWorld;
    }
}
