package nl.jk5.pumpkin.server.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.util.CharsetUtil;
import nl.jk5.pumpkin.api.mappack.*;
import nl.jk5.pumpkin.api.mappack.game.stat.StatEmitterConfig;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.exception.MappackLoadingException;
import nl.jk5.pumpkin.server.exception.MappackNotFoundException;
import nl.jk5.pumpkin.server.utils.FutureUtils;
import nl.jk5.pumpkin.server.utils.RegistryUtils;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HttpMappackWorld implements MappackWorld {

    private final Mappack mappack;
    private final int id;
    private final String name;
    private final boolean isDefault;
    private final GeneratorType generatorType;
    private final DimensionType dimensionType;
    private final long seed;
    private final PlayerLocation spawnPoint;
    private final GameMode gameMode;
    private final boolean generateStructures;
    private final int initialTime;
    private final String generatorOptions;

    public HttpMappackWorld(Mappack mappack, JsonObject data) {
        this.mappack = mappack;
        this.id = data.get("id").getAsInt();
        this.name = data.get("name").getAsString();
        this.isDefault = data.get("is_default").getAsBoolean();

        JsonObject settings = data.getAsJsonObject("settings");
        Optional<GeneratorType> generatorType = RegistryUtils.generatorTypeByName(settings.get("generator").getAsString());
        if(!generatorType.isPresent()){
            throw new IllegalArgumentException("HttpMappackWorld " + this.getId() + " has an invalid generator type: " + settings.get("generator").getAsString());
        }else{
            this.generatorType = generatorType.get();
        }

        Optional<DimensionType> dimensionType = RegistryUtils.dimensionTypeByName(settings.get("dimension").getAsString());
        if(!dimensionType.isPresent()){
            throw new IllegalArgumentException("HttpMappackWorld " + this.getId() + " has an invalid dimension type: " + settings.get("dimension").getAsString());
        }else{
            this.dimensionType = dimensionType.get();
        }

        String seedString = settings.get("seed").getAsString();
        long seed;
        try{
            seed = Long.parseLong(seedString);
        }catch(NumberFormatException e){
            seed = seedString.hashCode();
        }
        this.seed = seed;

        this.spawnPoint = PlayerLocation.fromJson(settings.getAsJsonObject("spawn"));

        Optional<GameMode> gameMode = RegistryUtils.gameModeByName(data.get("gamemode").getAsString());
        if(!gameMode.isPresent()){
            throw new IllegalArgumentException("HttpMappackWorld " + this.getId() + " has an invalid gamemode: " + data.get("gamemode").getAsString());
        }else{
            this.gameMode = gameMode.get();
        }

        this.generateStructures = data.get("generate_structures").getAsBoolean();
        this.initialTime = data.get("initial_time").getAsInt();
        this.generatorOptions = data.get("flat_generator_settings").getAsString();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Mappack getMappack() {
        return this.mappack;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public GeneratorType getGenerator() {
        return this.generatorType;
    }

    @Override
    public DimensionType getDimension() {
        return this.dimensionType;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public PlayerLocation getSpawnpoint() {
        return this.spawnPoint;
    }

    @Override
    public GameMode getGamemode() {
        return this.gameMode;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return this.generateStructures;
    }

    @Override
    public int getInitialTime() {
        return this.initialTime;
    }

    @Override
    public String getGeneratorOptions() {
        return this.generatorOptions;
    }

    @Override
    public Collection<Zone> getZones() {
        return Collections.emptyList();
    }

    @Override
    public Collection<StatEmitterConfig> getStatEmitters() {
        return Collections.emptyList();
    }

    @Override
    public Collection<WorldGamerule> getGamerules() {
        return Collections.emptyList();
    }

    @Override
    public Collection<WorldRevision> getRevisions() {
        return Collections.emptyList();
    }

    @Override
    public WorldRevision getCurrentRevision() {
        return null;
    }

    public static CompletableFuture<HttpMappackWorld> byId(Mappack mappack, int id){
        CompletableFuture<HttpMappackWorld> ret = new CompletableFuture<>();
        ListenableFuture<Response> future = Pumpkin.instance().getAsyncHttpClient().prepareGet("https://pumpkin.jk-5.nl/api/mappacks/" + mappack.getId() + "/" + id).execute();
        future.addListener(() -> {
            Optional<Response> result = FutureUtils.getResult(future, ret);
            if(!result.isPresent()){
                return;
            }
            Response response = result.get();

            if(response.getStatusCode() == 404){
                ret.completeExceptionally(new MappackNotFoundException());
                return;
            }

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(response.getResponseBody(CharsetUtil.UTF_8)).getAsJsonObject();
            if(!json.get("success").getAsBoolean()){
                ret.completeExceptionally(new MappackLoadingException(json.get("error").getAsString()));
                return;
            }

            JsonObject data = json.getAsJsonObject("data");
            ret.complete(new HttpMappackWorld(mappack, data));
        }, Pumpkin.instance().getAsyncExecutor());
        return ret;
    }
}
