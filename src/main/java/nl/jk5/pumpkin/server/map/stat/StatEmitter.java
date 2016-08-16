package nl.jk5.pumpkin.server.map.stat;

import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.game.stat.StatEmitterConfig;
import nl.jk5.pumpkin.api.mappack.game.stat.StatListener;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappackWorld;
import nl.jk5.pumpkin.server.sql.obj.DatabaseStatEmitter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.Optional;

public class StatEmitter implements StatListener {

    private final Cause cause = Cause.source(Pumpkin.instance().getPluginContainer()).build();

    private final MapWorld world;
    private final StatEmitterConfig config;

    private Location<World> location;

    public StatEmitter(MapWorld world, StatEmitterConfig config, Location<World> location) {
        this.world = world;
        this.config = config;
        this.location = location;
    }

    public void setEnabled(boolean enabled){
        if(enabled){
            this.location.setBlock(BlockState.builder().blockType(BlockTypes.REDSTONE_BLOCK).build(), cause);
        }else{
            this.location.setBlock(BlockState.builder().blockType(BlockTypes.COAL_BLOCK).build(), cause);
        }
    }

    public void save(){
        if(this.config instanceof DatabaseStatEmitter){
            try {
                Pumpkin.instance().getTableManager().statEmitterDao.createOrUpdate((DatabaseStatEmitter) this.config);
            }catch(SQLException ignored){}
        }
    }

    public static Optional<StatEmitter> create(MapWorld world, Location<World> location){
        if(!(world.getConfig() instanceof DatabaseMappackWorld)){
            return Optional.empty();
        }
        Optional<StatEmitter> emitterOpt = world.getStatEmitters().stream().filter(e -> e.getConfig().getWorld() == world.getConfig() && e.getConfig().getX() == location.getBlockX() && e.getConfig().getY() == location.getBlockY() && e.getConfig().getZ() == location.getBlockZ()).findFirst();
        if(emitterOpt.isPresent()){
            return Optional.empty();
        }
        DatabaseStatEmitter config = new DatabaseStatEmitter();
        config.setWorld((DatabaseMappackWorld) world.getConfig());
        config.setStat(null);
        config.setX(location.getBlockX());
        config.setY(location.getBlockY());
        config.setZ(location.getBlockZ());

        StatEmitter emitter = new StatEmitter(world, config, location);
        emitter.setEnabled(false);
        return Optional.of(emitter);
    }

    public static StatEmitter from(MapWorld world, StatEmitterConfig config) {
        return new StatEmitter(world, config, new Location<>(world.getWorld(), config.getX(), config.getY(), config.getZ()));
    }

    public MapWorld getWorld() {
        return world;
    }

    public StatEmitterConfig getConfig() {
        return config;
    }

    @Override
    public void onTrigger() {
        this.setEnabled(true);
        Sponge.getScheduler().createTaskBuilder()
                .delayTicks(10)
                .execute(() -> this.setEnabled(false))
                .submit(Pumpkin.instance());
    }

    @Override
    public void onEnable() {
        this.setEnabled(true);
    }

    @Override
    public void onDisable() {
        this.setEnabled(false);
    }
}
