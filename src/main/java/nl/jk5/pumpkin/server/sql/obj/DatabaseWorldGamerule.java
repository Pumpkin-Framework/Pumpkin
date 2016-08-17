package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.WorldGamerule;

@DatabaseTable(tableName = "world_gamerule")
public class DatabaseWorldGamerule implements WorldGamerule {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "world_id", foreign = true, foreignAutoRefresh = true)
    private DatabaseMappackWorld world;

    @DatabaseField
    private String name;

    @DatabaseField
    private String value;

    public int getId() {
        return id;
    }

    public DatabaseMappackWorld getWorld() {
        return world;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
