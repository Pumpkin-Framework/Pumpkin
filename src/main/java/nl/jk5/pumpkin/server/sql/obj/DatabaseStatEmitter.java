package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.game.stat.StatEmitterConfig;

@DatabaseTable(tableName = "stat_emitter")
public class DatabaseStatEmitter implements StatEmitterConfig {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "world_id")
    private DatabaseMappackWorld world;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "stat_id")
    private DatabaseMappackStat stat;

    @DatabaseField
    private int x;

    @DatabaseField
    private int y;

    @DatabaseField
    private int z;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public DatabaseMappackWorld getWorld() {
        return world;
    }

    @Override
    public DatabaseMappackStat getStat() {
        return stat;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    public void setWorld(DatabaseMappackWorld world) {
        this.world = world;
    }

    public void setStat(DatabaseMappackStat stat) {
        this.stat = stat;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
