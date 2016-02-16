package nl.jk5.pumpkin.server.map;

import com.flowpowered.math.vector.Vector3i;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Zone;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;

import java.util.Optional;

public final class MapEventListener {

    private final Pumpkin pumpkin;

    public MapEventListener(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event){
        //if(!event.isBedSpawn()) {
        //    Location<World> location = setSpawn(event.getToTransform(), event.getTargetEntity());
        //    event.setToTransform(event.getToTransform().setLocation(location));
        //}
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event){
        if(!(event.getCause().root() instanceof Player)){
            return;
        }
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetWorld());
        if(!mapWorld.isPresent()){
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Optional<Zone> zone = mapWorld.get().getConfig().getZones().stream()
                    .filter(c -> {
                        return contains(c.getStart(), c.getEnd(), transaction.getOriginal().getPosition());
                    })
                    .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                    .findFirst();

            if(zone.isPresent()){
                if(zone.get().getActionBlockBreak() != null && zone.get().getActionBlockBreak().equals("deny")){
                    transaction.setValid(false);
                    return;
                }else if(zone.get().getActionBlockBreak() != null && zone.get().getActionBlockBreak().equals("allow")){
                    return;
                }
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event){
        if(!(event.getCause().root() instanceof Player)){
            return;
        }
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetWorld());
        if(!mapWorld.isPresent()){
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Optional<Zone> zone = mapWorld.get().getConfig().getZones().stream()
                    .filter(c -> contains(c.getStart(), c.getEnd(), transaction.getOriginal().getPosition()))
                    .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                    .findFirst();

            if(zone.isPresent()){
                if(zone.get().getActionBlockPlace() != null && zone.get().getActionBlockPlace().equals("deny")){
                    transaction.setValid(false);
                    return;
                }else if(zone.get().getActionBlockPlace() != null && zone.get().getActionBlockPlace().equals("allow")){
                    return;
                }
            }
        }
    }

    private boolean contains(Vector3i start, Vector3i end, Vector3i point){
        Vector3i min = start.min(end);
        Vector3i max = start.max(end);
        if(point.getX() < min.getX() || point.getX() > max.getX()){
            return false;
        }
        if(point.getY() < min.getY() || point.getY() > max.getY()){
            return false;
        }
        if(point.getZ() < min.getZ() || point.getZ() > max.getZ()){
            return false;
        }
        return true;
    }
}
