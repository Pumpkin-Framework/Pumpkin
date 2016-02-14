package nl.jk5.pumpkin.server.sql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.PostgresDatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappack;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappackAuthor;
import nl.jk5.pumpkin.server.sql.obj.DatabaseMappackWorld;
import nl.jk5.pumpkin.server.sql.obj.DatabaseUser;
import org.postgresql.Driver;

import java.sql.SQLException;

public final class SqlTableManager {

    private final Pumpkin pumpkin;

    private ConnectionSource conn;

    //public Dao<DatabaseGroupPermission, Integer> groupPermissionDao;
    //public Dao<DatabaseUserPermission, Void> userPermissionDao;
    //public Dao<DatabaseZone, Integer> zoneDao;
    public Dao<DatabaseMappack, Integer> mappackDao;
    public Dao<DatabaseMappackAuthor, Integer> mappackAuthorDao;
    public Dao<DatabaseUser, Integer> userDao;
    private Dao<DatabaseMappackWorld, Integer> mappackWorldsDao;
    //private Dao<DatabaseWorldFile, Integer> mappackFilesDao;
    //private Dao<DatabaseMappackTeam, Integer> mappackTeamDao;
    //private Dao<DatabaseGamerule, Integer> gameruleDao;
    //public Dao<DatabaseGroup, Integer> groupsDao;
    //private Dao<DatabaseGroupMembership, Integer> userGroupDao;
    //private Dao<DatabaseGame, Integer> gameDao;
    //private Dao<DatabaseGameEvent, Integer> gameEventDao;

    public SqlTableManager(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    public void setupTables(){
        try{
            this.conn = this.getConnectionSource();

            //groupPermissionDao = createTable(DatabaseGroupPermission.class);
            //userPermissionDao = createTable(DatabaseUserPermission.class);
            //zoneDao = createTable(DatabaseZone.class);
            this.mappackDao = createTable(DatabaseMappack.class);
            this.mappackAuthorDao = createTable(DatabaseMappackAuthor.class);
            this.userDao = createTable(DatabaseUser.class);
            this.mappackWorldsDao = createTable(DatabaseMappackWorld.class);
            //mappackFilesDao = createTable(DatabaseWorldFile.class);
            //mappackTeamDao = createTable(DatabaseMappackTeam.class);
            //gameruleDao = createTable(DatabaseGamerule.class);
            //groupsDao = createTable(DatabaseGroup.class);
            //userGroupDao = createTable(DatabaseGroupMembership.class);
            //gameDao = createTable(DatabaseGame.class);
            //gameEventDao = createTable(DatabaseGameEvent.class);

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private <T, ID> Dao<T, ID> createTable(Class<T> cls){
        Dao<T, ID> dao = null;
        try{
            dao = DaoManager.createDao(conn, cls);
            //TableUtils.createTableIfNotExists(conn, cls);
        }catch(SQLException e){
            Log.error("Error while creating table for " + cls.getSimpleName(), e);
        }
        return dao;
    }

    private ConnectionSource getConnectionSource() throws SQLException {
        PostgresDatabaseType type = new PostgresDatabaseType();
        type.setDriver(new Driver());
        return new DataSourceConnectionSource(this.pumpkin.getServiceManager().getSqlService().getDataSource("jdbc:postgresql://10.2.1.2/pumpkin?user=postgres&password=laserint"), type); //TODO: make this configurable
    }
}
