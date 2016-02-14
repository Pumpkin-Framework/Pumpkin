package nl.jk5.pumpkin.server.mappack;

import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappack;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MappackRegistry {

    private final Pumpkin pumpkin;

    public MappackRegistry(Pumpkin pumpkin){
        this.pumpkin = pumpkin;
    }

    public Optional<Mappack> byId(int id){
        try {
            return Optional.ofNullable(this.pumpkin.getTableManager().mappackDao.queryForId(id));
        } catch (SQLException e) {
            Log.warn("MappackRegistry could not query mappack with id " + id, e);
            return Optional.empty();
        }
    }

    public Optional<Mappack> byName(String name){
        List<DatabaseMappack> mappacks;
        try {
            mappacks = this.pumpkin.getTableManager().mappackDao.queryForEq("name", name);
        } catch (SQLException e) {
            Log.warn("MappackRegistry could not query mappack with name " + name, e);
            return Optional.empty();
        }
        if(mappacks.size() == 0){
            return Optional.empty();
        }else{
            return Optional.of(mappacks.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Mappack> getAllMappacks(){
        try {
            return (Collection) this.pumpkin.getTableManager().mappackDao.queryForAll();
        } catch (SQLException e) {
            Log.warn("MappackRegistry could not query mappacks", e);
            return Collections.emptyList();
        }
    }
}
