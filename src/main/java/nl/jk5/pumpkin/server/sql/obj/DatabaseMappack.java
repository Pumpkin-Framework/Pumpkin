package nl.jk5.pumpkin.server.sql.obj;

import com.google.common.base.MoreObjects;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.MappackAuthor;
import nl.jk5.pumpkin.api.mappack.MappackTeam;
import nl.jk5.pumpkin.api.mappack.MappackWorld;
import nl.jk5.pumpkin.api.mappack.game.stat.StatConfig;

import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "mappack")
@SuppressWarnings("unused")
public class DatabaseMappack implements Mappack {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String description;

    @DatabaseField(columnName = "public")
    private boolean isPublic;

    @DatabaseField
    private String version;

    @DatabaseField(columnName = "description_by", foreign = true)
    private DatabaseUser descriptionBy;

    @DatabaseField(columnName = "description_updated")
    private Date descriptionUpdated;

    @ForeignCollectionField
    private ForeignCollection<DatabaseMappackAuthor> authors;

    @ForeignCollectionField
    private ForeignCollection<DatabaseMappackTeam> teams;

    @ForeignCollectionField
    private ForeignCollection<DatabaseMappackWorld> worlds;

    @ForeignCollectionField
    private ForeignCollection<DatabaseMappackStat> stats;

    //@ForeignCollectionField
    //private ForeignCollection<DatabaseGamerule> gamerules;

    public DatabaseMappack() {
    }

    public DatabaseMappack(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<MappackAuthor> getAuthors() {
        return ((Collection) this.authors);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<MappackWorld> getWorlds() {
        return ((Collection) this.worlds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<MappackTeam> getTeams() {
        return ((Collection) teams);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<StatConfig> getStats() {
        return ((Collection) stats);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("isPublic", isPublic)
                .add("version", version)
                .add("worlds", worlds)
                .toString();
    }
}
