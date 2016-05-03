package nl.jk5.pumpkin.server.map;

import com.google.common.base.Preconditions;
import nl.jk5.pumpkin.api.mappack.*;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.world.gen.empty.DummyVoidGenerator;
import nl.jk5.pumpkin.server.world.gen.empty.VoidWorldGeneratorModifier;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
            HttpClient client = HttpClientBuilder.create().build();

            List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();
            world.getFiles().stream().filter(WorldFile::isRequired).forEach(file -> {
                // TODO: 13-2-16 maybe even generate level.dat instead of downloading it
                downloadFutures.add(CompletableFuture.runAsync(() -> {
                    try{
                        Log.info("Downloading file " + file.getPath());
                        HttpGet req = new HttpGet("https://pumpkin.jk-5.nl/api/files/" + file.getFileId());
                        HttpResponse res = client.execute(req);
                        if(res.getStatusLine().getStatusCode() != 200){
                            throw new MapLoadingException("Got non-200 response code on downloading file " + file.getPath() + ": " + res.getStatusLine().getStatusCode() + ": " + res.getStatusLine().getReasonPhrase());
                        }
                        File dest = new File(mapDir, world.getName() + "/" + file.getPath());
                        if(!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        try(
                                InputStream content = res.getEntity().getContent();
                                DigestInputStream dis = new DigestInputStream(content, md)
                        ){
                            Files.copy(dis, dest.toPath());
                            String downloadedChecksum = bytesToHex(md.digest());
                            if(file.getChecksum().equals(downloadedChecksum)){
                                Log.info("Downloaded file " + file.getPath() + " (checksums matched)");
                            }else{
                                throw new MapLoadingException("Could not download file " + file.getPath() + ". Checksums did not match\n" +
                                        "Calculated: " + downloadedChecksum + '\n' +
                                        "Should be:  " + file.getChecksum()
                                );
                            }
                        }
                    }catch(FileAlreadyExistsException e){
                        Log.warn("File " + file.getPath() + " already exists");
                    }catch(IOException e){
                        throw new MapLoadingException("Error while downloading file " + file.getPath(), e);
                    }catch(NoSuchAlgorithmException e){
                        throw new MapLoadingException("md5 algorithm not found. Should not be able to happen", e);
                    }
                }, asyncExecutor));
            });
            CompletableFuture<Void> downloadFuture = CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[downloadFutures.size()]));

            CompletableFuture<WorldProperties> prepareFuture = downloadFuture.thenApplyAsync((downloadRes) -> {
                //TODO: 9-2-16 flat generator settings
                WorldCreationSettings.Builder settingsBuilder = WorldCreationSettings.builder()
                        .dimension(world.getDimension())
                        .enabled(true)
                        .gameMode(world.getGamemode())
                        .name("maps/map-" + id + "/" + world.getName())
                        .seed(world.getSeed())
                        .generateSpawnOnLoad(false)
                        .usesMapFeatures(world.shouldGenerateStructures());

                if(world.getGenerator() instanceof DummyVoidGenerator){
                    settingsBuilder.generatorModifiers(new VoidWorldGeneratorModifier());
                }else{
                    settingsBuilder.generator(world.getGenerator());
                }

                WorldCreationSettings settings = settingsBuilder.build();

                Optional<WorldProperties> properties = this.pumpkin.getGame().getServer().createWorldProperties(settings);
                if(!properties.isPresent()){
                    Log.warn("World properties could not be loaded. Map loading canceled (mappack id: " + mappack.getId() + ")(map id: " + id + ")(world id: " + world.getId() + ")");
                    destroyMap(map);
                    throw new MapLoadingException("World properties could not be loaded");
                }
                properties.get().setWorldTime(world.getInitialTime());
                properties.get().setSpawnPosition(world.getSpawnpoint().toVector3i());

                return properties.get();
            }, asyncExecutor);

            loadFutures.add(prepareFuture.thenApplyAsync((properties) -> {
                Optional<World> generatedWorld = this.pumpkin.getGame().getServer().loadWorld(properties);
                if(!generatedWorld.isPresent()){
                    Log.warn("World could not be generated. Map loading canceled (mappack id: " + mappack.getId() + ")(map id: " + id + ")(world id: " + world.getId() + ")");
                    destroyMap(map);
                    throw new MapLoadingException("World could not be generated");
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
