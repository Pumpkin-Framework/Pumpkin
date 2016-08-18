package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.WorldFile;
import nl.jk5.pumpkin.api.mappack.WorldRevision;

import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "world_revision")
public class DatabaseWorldRevision implements WorldRevision {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "world_id", foreign = true, foreignAutoRefresh = true)
    private DatabaseMappackWorld world;

    @DatabaseField
    private Date created;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<DatabaseWorldFile> files;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<WorldFile> getFiles() {
        return ((Collection) files);
    }
}
