package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.game.stat.StatConfig;

@DatabaseTable(tableName = "mappack_stat")
public class DatabaseMappackStat implements StatConfig {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "map_id")
    private DatabaseMappack mappack;

    @DatabaseField
    private String type;

    @DatabaseField
    private String arguments;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Mappack getMappack() {
        return mappack;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getArguments() {
        return arguments;
    }
}
