package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.WorldFile;

@DatabaseTable(tableName = "world_file")
public class DatabaseWorldFile implements WorldFile {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "world_id", foreign = true, foreignAutoRefresh = true)
    private DatabaseMappackWorld world;

    @DatabaseField
    private String path;

    @DatabaseField(columnName = "file_id")
    private String fileId;

    @DatabaseField(defaultValue = "TRUE")
    private boolean required;

    @DatabaseField(canBeNull = false, width = 32)
    private String checksum;

    public int getId() {
        return id;
    }

    public DatabaseMappackWorld getWorld() {
        return world;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFileId() {
        return fileId;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }
}
