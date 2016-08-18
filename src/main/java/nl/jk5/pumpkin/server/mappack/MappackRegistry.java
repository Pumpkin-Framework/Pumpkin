package nl.jk5.pumpkin.server.mappack;

import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.exception.MappackLoadingException;
import nl.jk5.pumpkin.server.exception.MappackNotFoundException;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappack;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MappackRegistry {

    private final Pumpkin pumpkin;

    public MappackRegistry(Pumpkin pumpkin){
        this.pumpkin = pumpkin;
    }

    public CompletableFuture<Mappack> byId(int id){
        CompletableFuture<Mappack> ret = new CompletableFuture<>();
        Pumpkin.instance().getAsyncExecutor().submit(() -> {
            try {
                DatabaseMappack mappack = this.pumpkin.getTableManager().mappackDao.queryForId(id);
                if(mappack == null){
                    ret.completeExceptionally(new MappackNotFoundException());
                }else{
                    Log.info("Completing");
                    ret.complete(mappack);
                }
            } catch (SQLException e) {
                ret.completeExceptionally(new MappackLoadingException("MappackRegistry could not find mappack with id " + id, e));
            }
        });
        return ret;
    }

    public CompletableFuture<Mappack> byName(String name){
        CompletableFuture<Mappack> ret = new CompletableFuture<>();
        Pumpkin.instance().getAsyncExecutor().submit(() -> {
            try {
                List<DatabaseMappack> mappacks = this.pumpkin.getTableManager().mappackDao.queryForEq("name", name);
                if(mappacks.size() == 0){
                    ret.completeExceptionally(new MappackNotFoundException());
                }else{
                    ret.complete(mappacks.get(0));
                }
            } catch (SQLException e) {
                ret.completeExceptionally(new MappackLoadingException("MappackRegistry could not find mappack with name " + name, e));
            }
        });
        return ret;
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<Collection<Mappack>> getAllMappacks(){
        CompletableFuture<Collection<Mappack>> ret = new CompletableFuture<>();
        Pumpkin.instance().getAsyncExecutor().submit(() -> {
            try {
                List<DatabaseMappack> mappacks = this.pumpkin.getTableManager().mappackDao.queryForAll();
                ret.complete((Collection) mappacks);
            } catch (SQLException e) {
                ret.completeExceptionally(new MappackLoadingException("MappackRegistry could not query mappacks", e));
            }
        });
        return ret;
    }
}
