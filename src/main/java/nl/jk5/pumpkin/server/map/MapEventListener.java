package nl.jk5.pumpkin.server.map;

import com.flowpowered.math.vector.Vector3d;
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
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MapEventListener {

    private static final List<BlockDestroyWatcher> watchers = new LinkedList<>();

    private final Pumpkin pumpkin;
    private final java.util.Map<Map, java.util.Map<Player, FastRespawnInfo>> fastRespawnPlayers = new HashMap<>();

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
        // TODO: Is getting the world from the player the nicest way to handle this?
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                        //noinspection UnnecessaryContinue
                        continue;
                    }else if(zone.get().getActionBlockBreak() != null && zone.get().getActionBlockBreak().equals("allow")){
                        //noinspection UnnecessaryContinue
                        continue;
                    }
                }
            }else{
                //TODO: add edit mode, to bypass this
                //transaction.setValid(false);
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player){
        // TODO: Is getting the world from the player the nicest way to handle this?
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                //TODO: add edit mode, to bypass this
                //transaction.setValid(false);
            }
        }
    }

    @Listener
    public void onAttack(DamageEntityEvent event) {
        Optional<DamageSource> damageSourceOpt = event.getCause().first(DamageSource.class);
        if (!damageSourceOpt.isPresent()) {
            return;
        }
        DamageSource damageSource = damageSourceOpt.get();

        Optional<MapWorld> mapWorldOpt = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetEntity().getWorld());
        if (!mapWorldOpt.isPresent()) {
            return;
        }
        MapWorld world = mapWorldOpt.get();
        Map map = world.getMap();
        boolean ignore = false;

        // Handle player -> entity when player not in game
        if (damageSource instanceof EntityDamageSource) { // If the damage is caused by an entity
            EntityDamageSource entityDamageSource = (EntityDamageSource) damageSource;
            if (entityDamageSource.getSource() instanceof Player) { // A player attacked something
                Player sourcePlayer = (Player) entityDamageSource.getSource();
                if (!map.isInActiveGame(sourcePlayer)) { // If the player is not in an active game, cancel the damage
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Handle void -> player when player not in game
        if (event.getTargetEntity() instanceof Player) {
            Player target = (Player) event.getTargetEntity();
            if (damageSource.getType() == DamageTypes.VOID) { // Void damage
                if (!map.isInActiveGame(target)) { // If the player is not in an active game, respawn it quickly
                    target.setLocationAndRotation(world.getSpawnPoint(), world.getConfig().getSpawnpoint().getRotation());
                    event.setCancelled(true);
                    return;
                } else {
                    if(fastRespawnPlayers.containsKey(map) && fastRespawnPlayers.get(map).containsKey(event.getTargetEntity())){ // If the player is fast respawning, do not apply void damage, so he doesn't die twice
                        FastRespawnInfo respawnInfo = fastRespawnPlayers.get(map).get(event.getTargetEntity());
                        if(!respawnInfo.isRestored()){
                            event.setCancelled(true);
                            return;
                        }
                    }
                    // Let the void damage happen so the player does not fall forever
                    ignore = true;
                }
            }
        }

        // Handle * -> player when player not infastRespawnPlayers game
        if (!ignore && event.getTargetEntity() instanceof Player) {
            Player target = (Player) event.getTargetEntity();
            if (!map.isInActiveGame(target)) {
                event.setCancelled(true);
                return;
            }
        }

        // Handle player -> player
        if (!ignore && event.getTargetEntity() instanceof Player && damageSource instanceof EntityDamageSource && ((EntityDamageSource) damageSource).getSource() instanceof Player) {
            Player target = (Player) event.getTargetEntity();
            Player source = (Player) ((EntityDamageSource) damageSource).getSource();

            boolean targetActive = map.isInActiveGame(target);
            boolean sourceActive = map.isInActiveGame(source);

            // Handle a spectator attacking a player
            if (targetActive && !sourceActive) {
                event.setCancelled(true);
                source.sendMessage(Text.of(TextColors.RED, "Please don't attack players that are in game"));
                return;
            }

            // Handle the target being inactive
            if (!targetActive) {
                event.setCancelled(true);
                return;
            }

            // At this point both players are active

            Optional<Team> sourceTeam = map.getPlayerTeam(source);
            Optional<Team> targetTeam = map.getPlayerTeam(target);

            // When both players are in a team
            if (sourceTeam.isPresent() && targetTeam.isPresent()) {

                // If the players are in the same team
                if (sourceTeam.get() == targetTeam.get()) {

                    // Handle friendly fire
                    if (sourceTeam.get().isFriendlyFireEnabled()) {
                        event.setCancelled(true);
                        //noinspection UnnecessaryReturnStatement
                        return;
                    }
                }
            }
        }

        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            if (event.willCauseDeath()) {
                Optional<Team> t = map.getPlayerTeam(player);
                if (t.isPresent() && ((MapTeam) t.get()).getFastRespawnTimer() >= 0) {
                    MapTeam team = ((MapTeam) t.get());
                    FastRespawnInfo respawnInfo = new FastRespawnInfo(player, map, team.getFastRespawnTimer());

                    java.util.Map<Player, FastRespawnInfo> infoMap;
                    if(this.fastRespawnPlayers.containsKey(map)){
                        infoMap = this.fastRespawnPlayers.get(map);
                    }else{
                        infoMap = new HashMap<>();
                        this.fastRespawnPlayers.put(map, infoMap);
                    }

                    infoMap.put(player, respawnInfo);

                    player.getInventory().slots().forEach(slot -> {
                        Optional<ItemStack> stack = slot.peek();
                        if (stack.isPresent()) {
                            Vector3d position = player.getLocation().getPosition();
                            position = position.add(0, 1.3, 0);
                            Item item = (Item) player.getWorld().createEntity(EntityTypes.ITEM, position);
                            item.offer(Keys.REPRESENTED_ITEM, stack.get().createSnapshot());

                            double m1 = player.getRandom().nextDouble() * 0.5d;
                            double m2 = player.getRandom().nextDouble() * Math.PI * 2d;
                            double mX = -Math.sin(m2) * m1;
                            double mZ = Math.cos(m2) * m1;
                            item.setVelocity(new Vector3d(mX, 0.2, mZ));

                            player.getWorld().spawnEntity(item);
                            slot.clear();
                        }
                    });
                    player.getInventory().clear(); // TODO: 3-3-16 Fix armor not being dropped

                    map.send(Text.of(player.getName(), " died")); // TODO: 3-3-16 Improve death message

                    // TODO: 2-3-16 Prevent spectator player tracking
                    // TODO: 2-3-16 World border red warning
                    // TODO: 2-3-16 Count as a death in the death system
                    // TODO: 2-3-16 Maybe even fire the respawn event?

                    // TODO: 3-3-16 If a player switches a map while doing fast respawn, its walk speed stays 0

                    respawnInfo.setOldFlySpeed(player.get(Keys.FLYING_SPEED).get());
                    respawnInfo.setOldWalkSpeed(player.get(Keys.WALKING_SPEED).get());
                    respawnInfo.setOldGameMode(player.get(Keys.GAME_MODE).get());

                    event.setCancelled(true);
                    player.offer(Keys.HEALTH, player.maxHealth().get());
                    player.offer(Keys.FOOD_LEVEL, player.foodLevel().getMaxValue());
                    player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
                    player.offer(Keys.FLYING_SPEED, 0d);
                    player.offer(Keys.WALKING_SPEED, 0d);
                    player.offer(Keys.FALL_DISTANCE, 0f);

                    Title.Builder titleBuilder = Title.builder().title(Text.of(TextColors.RED, "You died!")).fadeIn(0).fadeOut(0).stay(10);
                    Task[] task = new Task[1];
                    Task.Builder builder = Sponge.getScheduler().createTaskBuilder().intervalTicks(2).execute(() -> {
                        int ticksLeft = respawnInfo.getAndDecrement();
                        if(ticksLeft <= 0){
                            task[0].cancel();
                        }
                        if (!player.isOnline()) {
                            return;
                        }
                        Optional<Map> currentMap = pumpkin.getMapRegistry().getMap(player);
                        if(currentMap.isPresent() && currentMap.get() != map){
                            // Player is in a different map. Ignore it
                            return;
                        }
                        if(respawnInfo.mayRespawn()){
                            infoMap.remove(player);
                            respawnInfo.restore();
                            PlayerLocation spawnPoint = getSpawnPoint(player);
                            player.setLocationAndRotation(spawnPoint.toLocation(player.getWorld()), spawnPoint.getRotation());
                            return;
                        }
                        Title title = titleBuilder.subtitle(Text.of(TextColors.GOLD, "Respawning in " + decimal((double) ticksLeft / 10d) + " seconds")).build();
                        player.sendTitle(title);
                    });
                    task[0] = builder.submit(Pumpkin.instance());
                }
            }
        }
    }

    @Listener
    public void onDataHolderChanged(ChangeDataHolderEvent event){
        //TODO: handle food drain. Cancel when needed
    }

    public void onPlayerJoinMap(Player player, DefaultMap map) {
        if(this.fastRespawnPlayers.containsKey(map) && this.fastRespawnPlayers.get(map).containsKey(player)){
            java.util.Map<Player, FastRespawnInfo> infoMap = this.fastRespawnPlayers.get(map);
            FastRespawnInfo respawnInfo = infoMap.get(player);

            Optional<Map> currentMap = pumpkin.getMapRegistry().getMap(player);
            if(currentMap.isPresent() && currentMap.get() != map){
                // Player is in a different map. Ignore it
                return;
            }
            if(!respawnInfo.isRestored()){
                infoMap.remove(player);
                respawnInfo.restore();
                PlayerLocation spawnPoint = getSpawnPoint(player);
                player.setLocationAndRotation(spawnPoint.toLocation(player.getWorld()), spawnPoint.getRotation());
            }
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

    @Listener
    public void watcherBlockBreak(ChangeBlockEvent.Break event, @First World world){
        Optional<MapWorld> mapWorld = pumpkin.getMapRegistry().getMapWorld(world);  //TODO: Is this way of getting the world correct?
        if(!mapWorld.isPresent()){
            return;
        }
        for(Transaction<BlockSnapshot> transaction : event.getTransactions()){
            Vector3i position = transaction.getOriginal().getPosition();
            BlockDestroyWatcher watcher = new BlockDestroyWatcher(mapWorld.get(), position.getX(), position.getY(), position.getZ());
            Optional<BlockDestroyWatcher> match = watchers.stream().filter(w -> w.equals(watcher)).findAny();
            if(match.isPresent()){
                match.get().call();
            }
        }
    }

    public static void registerBlockDestroyWatcher(MapWorld world, int x, int y, int z) {
        BlockDestroyWatcher watcher = new BlockDestroyWatcher(world, x, y, z);
        watchers.add(watcher);
    }

    private static class BlockDestroyWatcher {

        private final MapWorld world;
        private final int x;
        private final int y;
        private final int z;
        private final String name;

        public BlockDestroyWatcher(MapWorld world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = "block_break_" + world.getConfig().getName() + "_" + x + "," + y + "," + z;
        }

        public void call(){
            world.getMap().getMachine().signal(this.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            //noinspection ConstantConditions
            if (o == null || getClass() != o.getClass()) return false;

            BlockDestroyWatcher watcher = (BlockDestroyWatcher) o;

            if (x != watcher.x) return false;
            if (y != watcher.y) return false;
            if (z != watcher.z) return false;
            //noinspection SimplifiableIfStatement
            if (!world.equals(watcher.world)) return false;
            return name.equals(watcher.name);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    private class FastRespawnInfo {

        private final Player player;
        private final Map map;
        private final AtomicInteger timeLeft;

        private boolean restored = false;
        private double oldFlySpeed;
        private double oldWalkSpeed;
        @Nullable private GameMode oldGameMode;
        private boolean mayRespawn = false;

        public FastRespawnInfo(Player player, Map map, int respawnTime) {
            this.player = player;
            this.map = map;
            this.timeLeft = new AtomicInteger(respawnTime * 10);
        }

        public boolean isRestored() {
            return restored;
        }

        public void setRestored(boolean restored) {
            this.restored = restored;
        }

        public Player getPlayer() {
            return player;
        }

        public void setOldFlySpeed(double oldFlySpeed) {
            this.oldFlySpeed = oldFlySpeed;
        }

        public void setOldWalkSpeed(double oldWalkSpeed) {
            this.oldWalkSpeed = oldWalkSpeed;
        }

        public void setOldGameMode(GameMode oldGameMode) {
            this.oldGameMode = oldGameMode;
        }

        public int getAndDecrement(){
            int ret = this.timeLeft.getAndDecrement();
            if(ret <= 0){
                this.mayRespawn = true;
            }
            return ret;
        }

        public Map getMap() {
            return map;
        }

        public void restore(){
            this.setRestored(true);

            if(oldGameMode != null) player.offer(Keys.GAME_MODE, oldGameMode);
            player.offer(Keys.FLYING_SPEED, oldFlySpeed);
            player.offer(Keys.WALKING_SPEED, oldWalkSpeed);
            player.offer(Keys.HEALTH, player.maxHealth().get());
            player.offer(Keys.FOOD_LEVEL, player.foodLevel().getMaxValue());
            player.offer(Keys.FALL_DISTANCE, 0f);
        }

        public boolean mayRespawn() {
            return mayRespawn;
        }
    }
}
