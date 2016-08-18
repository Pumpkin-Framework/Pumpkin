package nl.jk5.pumpkin.server.map;

import com.google.common.base.Preconditions;
import nl.jk5.pumpkin.api.mappack.*;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class MapRegistry {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private final AtomicInteger nextMapId = new AtomicInteger(0);

    private final List<Map> maps = new ArrayList<>();
    private final java.util.Map<World, MapWorld> mapWorlds = new HashMap<>();

    private final Pumpkin pumpkin;

    private Map lobby;

    public MapRegistry(Pumpkin pumpkin){
        this.pumpkin = pumpkin;

        this.pumpkin.getGame().getScheduler().createTaskBuilder()
                .intervalTicks(1)
                .execute(this::tick)
                .submit(this.pumpkin);
    }

    public CompletableFuture<Map> load(Mappack mappack){
        return load(mappack, false);
    }

    public CompletableFuture<Map> load(Mappack mappack, boolean loadWorldAsync){
        Preconditions.checkNotNull(mappack, "mappack");
        SpongeExecutorService asyncExecutor = this.pumpkin.getGame().getScheduler().createAsyncExecutor(this.pumpkin);
        SpongeExecutorService syncExecutor = this.pumpkin.getGame().getScheduler().createSyncExecutor(this.pumpkin);
        List<CompletableFuture<DefaultMapWorld>> loadFutures = new ArrayList<>();

        int id = nextMapId.getAndIncrement();
        File mapDir = new File("world/maps/map-" + id);
        DefaultMap map = new DefaultMap(mappack, this.pumpkin, mapDir);
        Log.info("Loading mappack " + mappack.getName() + " (mappack id: " + mappack.getId() + ")(map id: " + id + ")");

        for (MappackWorld world : mappack.getWorlds()) {
            List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();

            world.getCurrentRevision().getFiles().stream().filter(WorldFile::isRequired).forEach(file -> {
                // TODO: 13-2-16 maybe even generate level.dat instead of downloading it
                CompletableFuture<Void> retrieveFuture = new CompletableFuture<>();
                downloadFutures.add(retrieveFuture);
                Log.info("Downloading file " + file.getPath());
                ListenableFuture<Response> f = this.pumpkin.getAsyncHttpClient().prepareGet("https://pumpkin.jk-5.nl/api/mappacks/" + mappack.getId() + "/worlds/" + world.getId() + "/files/" + file.getPath()).execute();
                f.addListener(() -> {
                    Response response = null;
                    try {
                        response = f.get();
                    } catch (InterruptedException ignored) {
                        return;
                    } catch (ExecutionException e) {
                        retrieveFuture.completeExceptionally(e.getCause());
                        return;
                    }

                    if(response.getStatusCode() != 200){
                        retrieveFuture.completeExceptionally(new MapLoadingException("Got non-200 response code on downloading file " + file.getPath() + ": " + response.getStatusCode() + ": " + response.getStatusText()));
                        return;
                    }

                    try {
                        File dest = new File(mapDir, world.getName() + "/" + file.getPath());
                        if(!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
                        try(InputStream content = response.getResponseBodyAsStream()){
                            Files.copy(content, dest.toPath());
                            Log.info("Downloaded file " + file.getPath());
                        }
                    } catch(FileAlreadyExistsException e){
                        Log.warn("File " + file.getPath() + " already exists");
                    } catch (IOException e) {
                        retrieveFuture.completeExceptionally(new MapLoadingException("Error while downloading file " + file.getPath(), e));
                        return;
                    }
                    retrieveFuture.complete(null);
                }, this.pumpkin.getAsyncExecutor());

            });
            CompletableFuture<Void> downloadFuture = CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[downloadFutures.size()]));

            CompletableFuture<WorldProperties> prepareFuture = downloadFuture.thenApplyAsync((downloadRes) -> {
                //TODO: 9-2-16 flat generator settings

                try {
                    //TODO: remove hardcoded overworld
                    WorldProperties properties = this.pumpkin.getGame().getServer().createWorldProperties("maps/map-" + id + "/" + world.getName(), WorldArchetypes.OVERWORLD);
                    //properties.setDimensionType(world.getDimension()); //TODO: Unable to change dimension type
                    properties.setGenerateSpawnOnLoad(false);
                    properties.setGameMode(world.getGamemode());
                    //properties.setSeed(world.getSeed()); //TODO: Unable to change the seed
                    properties.setGenerateSpawnOnLoad(false);
                    properties.setMapFeaturesEnabled(world.shouldGenerateStructures());

                    for (WorldGamerule gamerule : world.getGamerules()) {
                        properties.setGameRule(gamerule.getName(), gamerule.getValue());
                    }

                    //if(world.getGenerator() instanceof DummyVoidGenerator){
                    //    properties.setGeneratorModifiers(Collections.singletonList(new VoidWorldGeneratorModifier()));
                    //}else{
                        properties.setGeneratorType(world.getGenerator());
                    //}

                    properties.setWorldTime(world.getInitialTime());
                    properties.setSpawnPosition(world.getSpawnpoint().toVector3i());
                    return properties;
                } catch (IOException e) {
                    Log.warn("World could not be generated. Map loading canceled (mappack id: " + mappack.getId() + ")(map id: " + id + ")(world id: " + world.getId() + ")");
                    throw new MapLoadingException("World could not be generated", e);
                }
            }, asyncExecutor);

            loadFutures.add(prepareFuture.thenApplyAsync((properties) -> {
                Optional<World> generatedWorld = this.pumpkin.getGame().getServer().loadWorld(properties);
                if(!generatedWorld.isPresent()){
                    Log.warn("World could not be loaded. Map loading canceled (mappack id: " + mappack.getId() + ")(map id: " + id + ")(world id: " + world.getId() + ")");
                    destroyMap(map);
                    throw new MapLoadingException("World could not be loaded");
                }
                DefaultMapWorld mapWorld = new DefaultMapWorld(generatedWorld.get(), world, map);
                map.addWorld(mapWorld);
                this.mapWorlds.put(generatedWorld.get(), mapWorld);
                return mapWorld;
            }, loadWorldAsync ? asyncExecutor : syncExecutor));
        }

        CompletableFuture<Void> doneFuture = CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[loadFutures.size()]));
        return doneFuture.thenApplyAsync((worlds) -> {
            this.maps.add(map);
            return map;
        }, asyncExecutor);
    }

    private void tick(){
        this.maps.forEach(Map::tick);
    }

    private void destroyMap(Map map){
        map.getWorlds().stream().filter(m -> m instanceof DefaultMapWorld).map(m -> (DefaultMapWorld) m).forEach(m -> {
            m.destroy();
            this.mapWorlds.remove(m.getWorld());
        });
        this.maps.remove(map);
    }

    public Optional<MapWorld> getMapWorld(World world){
        return Optional.ofNullable(this.mapWorlds.get(world));
    }

    public Optional<Map> getMap(Player player){
        Optional<MapWorld> world = this.getMapWorld(player.getWorld());
        if(!world.isPresent()){
            return Optional.empty();
        }
        return Optional.of(world.get().getMap());
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void setLobby(Map lobby) {
        this.lobby = lobby;
    }

    public Map getLobby() {
        return lobby;
    }

    public void onSubtitleTick() {
        this.maps.forEach(m -> ((DefaultMap) m).onSubtitleTick());
    }
}
