package nl.jk5.pumpkin.server.map;

import com.flowpowered.math.vector.Vector3i;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.mappack.Zone;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.map.stat.StatEmitter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MapEventListener {

    private final Pumpkin pumpkin;

    public MapEventListener(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;

        pumpkin.game.getScheduler().createTaskBuilder()
                .async()
                .interval(1, TimeUnit.SECONDS)
                .execute(this::subtitleTick)
                .submit(pumpkin);
    }

    public void subtitleTick(){
        pumpkin.getMapRegistry().onSubtitleTick();
    }

    private PlayerLocation getSpawnPoint(Player player){
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(player.getWorld());
        if(mapWorld.get().getMap().isInActiveGame(player)) {
            Optional<Team> team = mapWorld.get().getMap().getPlayerTeam(player);
            if (!team.isPresent()) {
                return mapWorld.get().getConfig().getSpawnpoint();
            }
            Optional<PlayerLocation> spawnPoint = team.get().getSpawnPoint();
            if (!spawnPoint.isPresent()) {
                return mapWorld.get().getConfig().getSpawnpoint();
            }
            return spawnPoint.get();
        }else{
            return mapWorld.get().getConfig().getSpawnpoint();
        }
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event){
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getToTransform().getExtent());
        if(!mapWorld.isPresent()){
            return;
        }
        if(!event.isBedSpawn()){
            PlayerLocation spawnPoint = getSpawnPoint(event.getTargetEntity());
            event.setToTransform(event.getToTransform()
                    .setLocation(spawnPoint.toLocation(mapWorld.get().getWorld()))
                    .setRotation(spawnPoint.getRotation())
            );
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player){
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetWorld());
        if(!mapWorld.isPresent()){
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Optional<Zone> zone = mapWorld.get().getConfig().getZones().stream()
                    .filter(c -> contains(c.getStart(), c.getEnd(), transaction.getOriginal().getPosition()))
                    .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                    .findFirst();

            Optional<StatEmitter> statEmitter = mapWorld.get().getStatEmitters().stream().filter(em -> em.getConfig().getX() == transaction.getOriginal().getPosition().getX() && em.getConfig().getY() == transaction.getOriginal().getPosition().getY() && em.getConfig().getZ() == transaction.getOriginal().getPosition().getZ()).findAny();
            if(statEmitter.isPresent()){
                transaction.setValid(false);
                player.sendMessage(Text.of(TextColors.RED, "You may not destroy an active stat emitter. Do ", TextColors.GOLD, "/statemitter delete <x> <y> <z>", TextColors.RED, " to remove it"));
                continue;
            }

            if(mapWorld.get().getMap().isInActiveGame(player)){
                if(zone.isPresent()){
                    if(zone.get().getActionBlockBreak() != null && zone.get().getActionBlockBreak().equals("deny")){
                        transaction.setValid(false);
                        continue;
                    }else if(zone.get().getActionBlockBreak() != null && zone.get().getActionBlockBreak().equals("allow")){
                        continue;
                    }
                }
            }else{
                transaction.setValid(false);
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player){
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetWorld());
        if(!mapWorld.isPresent()){
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Optional<Zone> zone = mapWorld.get().getConfig().getZones().stream()
                    .filter(c -> contains(c.getStart(), c.getEnd(), transaction.getOriginal().getPosition()))
                    .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                    .findFirst();

            Optional<StatEmitter> statEmitter = mapWorld.get().getStatEmitters().stream().filter(em -> em.getConfig().getX() == transaction.getOriginal().getPosition().getX() && em.getConfig().getY() == transaction.getOriginal().getPosition().getY() && em.getConfig().getZ() == transaction.getOriginal().getPosition().getZ()).findAny();
            if(statEmitter.isPresent()){
                continue;
            }

            if(mapWorld.get().getMap().isInActiveGame(player)){
                if(zone.isPresent()){
                    if(zone.get().getActionBlockPlace() != null && zone.get().getActionBlockPlace().equals("deny")){
                        transaction.setValid(false);
                        return;
                    }else if(zone.get().getActionBlockPlace() != null && zone.get().getActionBlockPlace().equals("allow")){
                        return;
                    }
                }
            }else{
                transaction.setValid(false);
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
        boolean ignore = false;

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
        if(!ignore && event.getTargetEntity() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            if(damageSource.getType() == DamageTypes.VOID){ // Void damage
                if(!map.isInActiveGame(target)){ // If the player is not in an active game, respawn it quickly
                    target.setLocationAndRotation(world.getSpawnPoint(), world.getConfig().getSpawnpoint().getRotation());
                    event.setCancelled(true);
                    return;
                }else{
                    // Let the void damage happen so the player does not fall forever
                    ignore = true;
                }
            }
        }

        // Handle * -> player when player not in game
        if(!ignore && event.getTargetEntity() instanceof Player){
            Player target = (Player) event.getTargetEntity();
            if(!map.isInActiveGame(target)){
                event.setCancelled(true);
                return;
            }
        }

        // Handle player -> player
        if(!ignore && event.getTargetEntity() instanceof Player && damageSource instanceof EntityDamageSource && ((EntityDamageSource) damageSource).getSource() instanceof Player){
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

        if(event.getTargetEntity() instanceof Player){
            Player player = (Player) event.getTargetEntity();
            if(!event.willCauseDeath()){
                return;
            }

            double defaultFlySpeed = player.get(Keys.FLYING_SPEED).get();
            double defaultWalkSpeed = player.get(Keys.WALKING_SPEED).get();
            GameMode gameMode = player.get(Keys.GAME_MODE).get();

            // TODO: 2-3-16 Make respawn time configurable
            // TODO: 2-3-16 Only do fast respawn if allowed
            // TODO: 2-3-16 Drop all items
            // TODO: 2-3-16 Prevent spectator player tracking
            // TODO: 2-3-16 World border red warning
            // TODO: 2-3-16 Death message
            // TODO: 2-3-16 Count as a death in the death system
            // TODO: 2-3-16 Maybe even fire the respawn event?

            event.setCancelled(true);
            player.offer(Keys.HEALTH, player.maxHealth().get());
            player.offer(Keys.FOOD_LEVEL, player.foodLevel().getMaxValue());
            player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
            player.offer(Keys.FLYING_SPEED, 0d);
            player.offer(Keys.WALKING_SPEED, 0d);

            Title.Builder title = Title.builder().title(Text.of(TextColors.RED, "You died!")).fadeIn(0).fadeOut(0).stay(10);
            AtomicInteger timeLeft = new AtomicInteger(25 * 10);
            Task[] task = new Task[1];
            Task.Builder builder = Sponge.getScheduler().createTaskBuilder().intervalTicks(2).execute(() -> {
                int ticksLeft = timeLeft.getAndDecrement();
                if (!player.isOnline()) {
                    return;
                }
                if (ticksLeft == 0) {
                    task[0].cancel();
                    player.offer(Keys.HEALTH, player.maxHealth().get());
                    player.offer(Keys.FOOD_LEVEL, player.foodLevel().getMaxValue());
                    player.offer(Keys.GAME_MODE, gameMode);
                    player.offer(Keys.FLYING_SPEED, defaultFlySpeed);
                    player.offer(Keys.WALKING_SPEED, defaultWalkSpeed);
                    PlayerLocation spawnPoint = getSpawnPoint(player);
                    player.setLocationAndRotation(spawnPoint.toLocation(player.getWorld()), spawnPoint.getRotation());
                    return;
                }
                Title t = title.subtitle(Text.of(TextColors.GOLD, "Respawning in " + decimal((double) ticksLeft / 10d) + " seconds")).build();
                player.sendTitle(t);
            });
            task[0] = builder.submit(Pumpkin.instance());
        }
    }

    private String decimal(double in){
        String out = String.valueOf(in);
        if(!out.contains(".")){
            out = out + ".0";
        }
        return out;
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
