package nl.jk5.pumpkin.server.storage;

import com.google.gson.JsonObject;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.MappackAuthor;
import nl.jk5.pumpkin.api.mappack.MappackTeam;
import nl.jk5.pumpkin.api.mappack.MappackWorld;
import nl.jk5.pumpkin.api.mappack.game.stat.StatConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public final class HttpMappack implements Mappack {

    private final int id;
    private final String name;
    private final String version;

    private HttpMappack(JsonObject data){
        this.id = data.get("id").getAsInt();
        this.name = data.get("name").getAsString();
        this.version = data.get("version").getAsString();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<MappackAuthor> getAuthors() {
        return Collections.emptyList();
    }

    @Override
    public Collection<MappackWorld> getWorlds() {
        return Collections.emptyList();
    }

    @Override
    public Collection<MappackTeam> getTeams() {
        return Collections.emptyList();
    }

    @Override
    public Collection<StatConfig> getStats() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HttpMappack{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static CompletableFuture<Mappack> byId(int id){
        CompletableFuture<Mappack> ret = new CompletableFuture<>();
        //ListenableFuture<Response> future = Pumpkin.instance().getAsyncHttpClient().prepareGet("https://pumpkin.jk-5.nl/api/mappacks/" + id).execute();
        //handleMappackResponse(ret, future);
        return ret;
    }

    public static CompletableFuture<Mappack> byName(String name){
        CompletableFuture<Mappack> ret = new CompletableFuture<>();
        //ListenableFuture<Response> future = Pumpkin.instance().getAsyncHttpClient().prepareGet("https://pumpkin.jk-5.nl/api/mappacks/_find?name=" + name).execute();
        //handleMappackResponse(ret, future);
        return ret;
    }

    //private static void handleMappackResponse(CompletableFuture<Mappack> retFuture, ListenableFuture<Response> resFuture){
        //resFuture.addListener(() -> {
        //    Optional<Response> result = FutureUtils.getResult(resFuture, retFuture);
        //    if(!result.isPresent()){
        //        return;
        //    }
        //    Response response = result.get();
        //
        //    if(response.getStatusCode() == 404){
        //        retFuture.completeExceptionally(new MappackNotFoundException());
        //        return;
        //    }
        //
        //    JsonParser parser = new JsonParser();
        //    JsonObject json = parser.parse(response.getResponseBody(CharsetUtil.UTF_8)).getAsJsonObject();
        //    if(!json.get("success").getAsBoolean()){
        //        retFuture.completeExceptionally(new MappackLoadingException(json.get("error").getAsString()));
        //        return;
        //    }
        //
        //    JsonObject data = json.getAsJsonObject("data");
        //    retFuture.complete(new HttpMappack(data));
        //}, Pumpkin.instance().getAsyncExecutor());
    //}
}
