package nl.jk5.pumpkin.server.sql.obj;

import com.flowpowered.math.vector.Vector3i;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.Zone;

@DatabaseTable(tableName = "world_zone")
public class DatabaseZone implements Zone {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField(columnName = "world_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private DatabaseMappackWorld world;

    @DatabaseField
    private int priority;

    @DatabaseField
    private int x1;

    @DatabaseField
    private int y1;

    @DatabaseField
    private int z1;

    @DatabaseField
    private int x2;

    @DatabaseField
    private int y2;

    @DatabaseField
    private int z2;

    @DatabaseField(columnName = "action_blockplace")
    private String actionBlockPlace;

    @DatabaseField(columnName = "action_blockbreak")
    private String actionBlockBreak;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseMappackWorld getWorld() {
        return world;
    }

    public void setWorld(DatabaseMappackWorld world) {
        this.world = world;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getZ1() {
        return z1;
    }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getZ2() {
        return z2;
    }

    public void setZ2(int z2) {
        this.z2 = z2;
    }

    public String getActionBlockPlace() {
        return actionBlockPlace;
    }

    public void setActionBlockPlace(String actionBlockPlace) {
        this.actionBlockPlace = actionBlockPlace;
    }

    public String getActionBlockBreak() {
        return actionBlockBreak;
    }

    public void setActionBlockBreak(String actionBlockBreak) {
        this.actionBlockBreak = actionBlockBreak;
    }

    @Override
    public Vector3i getStart() {
        return new Vector3i(x1, y1, z1);
    }

    @Override
    public Vector3i getEnd() {
        return new Vector3i(x2, y2, z2);
    }
}
