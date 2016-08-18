package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.WorldFile;

@DatabaseTable(tableName = "world_file")
public class DatabaseWorldFile implements WorldFile {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "revision_id", foreign = true, foreignAutoRefresh = true)
    private DatabaseWorldRevision revision;

    @DatabaseField
    private String path;

    @DatabaseField(defaultValue = "TRUE")
    private boolean required;

    public int getId() {
        return id;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public String getPath() {
        return path;
    }
}
