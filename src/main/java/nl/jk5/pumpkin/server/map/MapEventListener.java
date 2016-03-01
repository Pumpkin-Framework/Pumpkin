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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
        Optional<DamageSource> damageSourceOpt = event.getCause().first(DamageSource.class);
        if(!damageSourceOpt.isPresent()){
            return;
        }
        DamageSource damageSource = damageSourceOpt.get();

        Optional<MapWorld> mapWorldOpt = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetEntity().getWorld());
        if(!mapWorldOpt.isPresent()){
            return;
        }
        MapWorld world = mapWorldOpt.get();
        Map map = world.getMap();

        // Handle player -> entity when player not in game
        if(damageSource instanceof EntityDamageSource){ // If the damage is caused by an entity
            EntityDamageSource entityDamageSource = (EntityDamageSource) damageSource;
            if(entityDamageSource.getSource() instanceof Player){ // A player attacked something
                Player sourcePlayer = (Player) entityDamageSource.getSource();
                if(!map.isInActiveGame(sourcePlayer)){ // If the player is not in an active game, cancel the damage
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Handle void -> player when player not in game
        if(event.getTargetEntity() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            if(damageSource.getType() == DamageTypes.VOID){ // Void damage
                if(!map.isInActiveGame(target)){ // If the player is not in an active game, respawn it quickly
                    target.setLocationAndRotation(world.getSpawnPoint(), world.getConfig().getSpawnpoint().getRotation());
                    event.setCancelled(true);
                    return;
                }else{
                    // Let the void damage happen so the player does not fall forever
                    return;
                }
            }
        }

        // Handle * -> player when player not in game
        if(event.getTargetEntity() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            if(!map.isInActiveGame(target)){
                event.setCancelled(true);
                return;
            }
        }

        // Handle player -> player
        if(event.getTargetEntity() instanceof Player && damageSource instanceof EntityDamageSource && ((EntityDamageSource) damageSource).getSource() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            Player source = (Player) ((EntityDamageSource) damageSource).getSource();

            boolean targetActive = map.isInActiveGame(target);
            boolean sourceActive = map.isInActiveGame(source);

            // Handle a spectator attacking a player
            if(targetActive && !sourceActive){
                event.setCancelled(true);
                source.sendMessage(Text.of(TextColors.RED, "Please don't attack players that are in game"));
                return;
            }

            // Handle the target being inactive
            if(!targetActive){
                event.setCancelled(true);
                return;
            }

            // At this point both players are active

            Optional<Team> sourceTeam = map.getPlayerTeam(source);
            Optional<Team> targetTeam = map.getPlayerTeam(target);

            // When both players are in a team
            if(sourceTeam.isPresent() && targetTeam.isPresent()){

                // If the players are in the same team
                if(sourceTeam.get() == targetTeam.get()){

                    // Handle friendly fire
                    if(sourceTeam.get().isFriendlyFireEnabled()){
                        event.setCancelled(true);
                        //noinspection UnnecessaryReturnStatement
                        return;
                    }
                }
            }
        }
    }

    @SuppressWarnings("RedundantIfStatement")
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
