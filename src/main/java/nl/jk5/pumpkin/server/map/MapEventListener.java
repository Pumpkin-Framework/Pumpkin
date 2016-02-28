package nl.jk5.pumpkin.server.map;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.mappack.Zone;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;

import java.util.Optional;

public final class MapEventListener {

    private final Pumpkin pumpkin;

    public MapEventListener(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event){
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getToTransform().getExtent());
        if(!mapWorld.isPresent()){
            return;
        }
        if(!event.isBedSpawn()){
            PlayerLocation spawn = mapWorld.get().getConfig().getSpawnpoint();
            event.setToTransform(event.getToTransform()
                    .setLocation(new Location<>(mapWorld.get().getWorld(), spawn.getX(), spawn.getY(), spawn.getZ()))
                    .setRotation(new Vector3d(spawn.getPitch(), spawn.getYaw(), 0))
            );
        }
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
                    .filter(c -> contains(c.getStart(), c.getEnd(), transaction.getOriginal().getPosition()))
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
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player){
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

    @Listener
    public void onAttack(DamageEntityEvent event){
        Optional<DamageSource> damageSource = event.getCause().first(DamageSource.class);
        if(!damageSource.isPresent()){
            return;
        }
        if(event.getTargetEntity() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            if(damageSource.get().getType() == DamageTypes.VOID){
                Optional<MapWorld> world = this.pumpkin.getMapRegistry().getMapWorld(target.getWorld());
                if(world.isPresent()){
                    //Faster void respawning
                    // TODO: 28-2-16 Disable this when player is ingame
                    target.setLocationAndRotation(world.get().getSpawnPoint(), world.get().getConfig().getSpawnpoint().getRotation());
                    event.setCancelled(true);
                    return;
                }else{
                    return;
                }
            }
            if(damageSource.get() instanceof EntityDamageSource){
                EntityDamageSource entitySource = (EntityDamageSource) damageSource.get();
                if(entitySource.getSource() instanceof Player){
                    Player source = (Player) entitySource.getSource();

                    // TODO: 27-2-16 Only check for friendly fire when the players are in an active game
                    Optional<Map> sourceMap = this.pumpkin.getMapRegistry().getMap(source);
                    Optional<Map> targetMap = this.pumpkin.getMapRegistry().getMap(target);

                    if(!sourceMap.isPresent() || !targetMap.isPresent() || sourceMap.get() != targetMap.get()){
                        event.setCancelled(true);
                        return;
                    }

                    Optional<Team> sourceTeam = sourceMap.get().getPlayerTeam(source);
                    Optional<Team> targetTeam = targetMap.get().getPlayerTeam(target);

                    if(!sourceTeam.isPresent() || !targetTeam.isPresent()){
                        // One of the players is not in a team. Spectators may not attack players, and players may not
                        // attack spectators. Cancel it
                        event.setCancelled(true);
                        return;
                    }

                    if(sourceTeam.get() != targetTeam.get() || !sourceTeam.get().isFriendlyFireEnabled()){
                        event.setCancelled(true);
                    }
                }else{
                    //TODO: if player is in a game, do not cancel the event
                    event.setCancelled(true);
                }
            }else{
                //TODO: if player is in a game, do not cancel the event
                event.setCancelled(true);
            }
        }else if(damageSource.get() instanceof EntityDamageSource && ((EntityDamageSource) damageSource.get()).getSource() instanceof Player){
            // Target is not a player, but source is a player
            //TODO: if player is in a game, do not cancel the event
            event.setCancelled(true);
        }else{
            // Target and source is not a player. Don't cancel the event
            return;
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
