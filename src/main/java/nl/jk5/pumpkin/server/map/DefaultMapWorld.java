package nl.jk5.pumpkin.server.map;

import com.google.common.base.MoreObjects;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.MappackWorld;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.map.stat.StatEmitter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public class DefaultMapWorld implements MapWorld {

    private final World world;
    private final MappackWorld mappackWorld;
    private final Map map;

    public DefaultMapWorld(World world, MappackWorld mappackWorld, Map map) {
        this.world = world;
        this.mappackWorld = mappackWorld;
        this.map = map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
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

    @Override
    public Map getMap() {
        return map;
    }

    public void onPlayerJoin(Player player){
        player.offer(Keys.GAME_MODE, this.getConfig().getGamemode());
    }

    public void onPlayerLeave(Player player){

    }

    public Collection<StatEmitter> getStatEmitters(){
        return this.map.getStatManager().getStatEmitters(this);
    }
}
