package nl.jk5.pumpkin.server.sql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.PostgresDatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.sql.obj.*;
import org.postgresql.Driver;

import java.sql.SQLException;

public final class SqlTableManager {

    private final Pumpkin pumpkin;
    private final String connectionString;

    private ConnectionSource conn;

    public Dao<DatabaseMappack, Integer> mappackDao;
    public Dao<DatabaseMappackAuthor, Integer> mappackAuthorDao;
    public Dao<DatabaseUser, Integer> userDao;
    public Dao<DatabaseMappackWorld, Integer> mappackWorldsDao;
    public Dao<DatabasePlayer, Integer> playerDao;
    public Dao<DatabaseZone, Integer> zoneDao;
    public Dao<DatabaseMappackTeam, Integer> mappackTeamDao;

    public SqlTableManager(Pumpkin pumpkin, String connectionString) {
        this.pumpkin = pumpkin;
        this.connectionString = connectionString;
    }

    public void connect(){
        PostgresDatabaseType type = new PostgresDatabaseType();
        type.setDriver(new Driver());
        try{
            this.conn = new DataSourceConnectionSource(this.pumpkin.getServiceManager().getSqlService().getDataSource(this.connectionString), type);
        }catch(SQLException e){
            Log.error("Could not connect to the database: " + e.getMessage());
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    public void setupTables(){
        //groupPermissionDao = createTable(DatabaseGroupPermission.class);
        //userPermissionDao = createTable(DatabaseUserPermission.class);
        //zoneDao = createTable(DatabaseZone.class);
        this.mappackDao = createTable(DatabaseMappack.class);
        this.mappackAuthorDao = createTable(DatabaseMappackAuthor.class);
        this.userDao = createTable(DatabaseUser.class);
        this.mappackWorldsDao = createTable(DatabaseMappackWorld.class);
        this.playerDao = createTable(DatabasePlayer.class);
        this.zoneDao = createTable(DatabaseZone.class);
        this.mappackTeamDao = createTable(DatabaseMappackTeam.class);
        //gameruleDao = createTable(DatabaseGamerule.class);
        //groupsDao = createTable(DatabaseGroup.class);
        //userGroupDao = createTable(DatabaseGroupMembership.class);
        //gameDao = createTable(DatabaseGame.class);
        //gameEventDao = createTable(DatabaseGameEvent.class);
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
}
