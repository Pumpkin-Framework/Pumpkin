package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "player")
public class DatabasePlayer {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(unique = true, canBeNull = false)
    private UUID uuid;

    @DatabaseField(width = 32, unique = true)
    private String username;

    @DatabaseField(columnName = "user_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private DatabaseUser user;

    @DatabaseField(columnName = "online_mode")
    private boolean onlineMode;

    @DatabaseField(canBeNull = false)
    private UUID server;

    @DatabaseField(columnName = "world_id")
    private int worldId;

    @DatabaseField
    private double x;

    @DatabaseField
    private double y;

    @DatabaseField
    private double z;

    @DatabaseField
    private float yaw;

    @DatabaseField
    private float pitch;

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public DatabaseUser getUser() {
        return user;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUser(DatabaseUser user) {
        this.user = user;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
    }

    public UUID getServer() {
        return server;
    }

    public void setServer(UUID server) {
        this.server = server;
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
