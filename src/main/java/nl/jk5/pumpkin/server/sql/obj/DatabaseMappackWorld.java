package nl.jk5.pumpkin.server.sql.obj;

import com.google.common.base.MoreObjects;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.MappackWorld;
import nl.jk5.pumpkin.api.mappack.WorldGamerule;
import nl.jk5.pumpkin.api.mappack.WorldRevision;
import nl.jk5.pumpkin.api.mappack.Zone;
import nl.jk5.pumpkin.api.mappack.game.stat.StatEmitterConfig;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.utils.RegistryUtils;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;

import java.util.Collection;
import java.util.Optional;

@DatabaseTable(tableName = "mappack_world")
public class DatabaseMappackWorld implements MappackWorld {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField(columnName = "mappack_id", foreign = true, foreignAutoRefresh = true)
    private DatabaseMappack map;

    @DatabaseField
    private String name;

    @DatabaseField
    private String generator;

    @DatabaseField
    private String dimension;

    @DatabaseField(columnName = "isdefault")
    private boolean isDefault;

    @DatabaseField(columnName = "spawn_x")
    private double spawnX;

    @DatabaseField(columnName = "spawn_y")
    private double spawnY;

    @DatabaseField(columnName = "spawn_z")
    private double spawnZ;

    @DatabaseField(columnName = "spawn_yaw")
    private float spawnYaw;

    @DatabaseField(columnName = "spawn_pitch")
    private float spawnPitch;

    @DatabaseField(width = 32, defaultValue = "0")
    private String seed;

    @DatabaseField(width = 16, defaultValue = "'adventure'")
    private String gamemode;

    @DatabaseField(columnName = "generate_structures", defaultValue = "TRUE")
    private boolean generateStructures;

    @DatabaseField(columnName = "initial_time", defaultValue = "0")
    private int initialTime;

    @DatabaseField(columnName = "flat_generator_settings", canBeNull = true, width = 256)
    private String generatorOptions;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<DatabaseZone> zones;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<DatabaseStatEmitter> statEmitters;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<DatabaseWorldGamerule> gamerules;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<DatabaseWorldRevision> revisions;

    @DatabaseField(columnName = "world_revision", foreign = true, foreignAutoRefresh = true)
    private DatabaseWorldRevision currentRevision;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public DatabaseMappack getMappack() {
        return map;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GeneratorType getGenerator() {
        Optional<GeneratorType> type = RegistryUtils.generatorTypeByName(this.generator);
        if(!type.isPresent()){
            throw new IllegalArgumentException("DatabaseMappackWorld " + this.getId() + " has an invalid generator type: " + this.generator);
        }else{
            return type.get();
        }
    }

    @Override
    public DimensionType getDimension() {
        Optional<DimensionType> type = RegistryUtils.dimensionTypeByName(this.dimension);
        if(!type.isPresent()){
            throw new IllegalArgumentException("DatabaseMappackWorld " + this.getId() + " has an invalid dimension type: " + this.dimension);
        }else{
            return type.get();
        }
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public long getSeed() {
        try{
            return Long.parseLong(seed);
        }catch(NumberFormatException e){
            return seed.hashCode();
        }
    }

    @Override
    public PlayerLocation getSpawnpoint(){
        return new PlayerLocation(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    @Override
    public GameMode getGamemode() {
        Optional<GameMode> gameMode = RegistryUtils.gameModeByName(this.gamemode);
        if(!gameMode.isPresent()){
            throw new IllegalArgumentException("DatabaseMappackWorld " + this.getId() + " has an invalid gamemode: " + this.gamemode);
        }else{
            return gameMode.get();
        }
    }

    @Override
    public boolean shouldGenerateStructures() {
        return generateStructures;
    }

    @Override
    public int getInitialTime() {
        return initialTime;
    }

    @Override
    public String getGeneratorOptions() {
        return generatorOptions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Zone> getZones() {
        return ((Collection) this.zones);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<StatEmitterConfig> getStatEmitters() {
        return ((Collection) this.statEmitters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<WorldGamerule> getGamerules() {
        return ((Collection) this.gamerules);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<WorldRevision> getRevisions() {
        return ((Collection) this.revisions);
    }

    @Override
    public DatabaseWorldRevision getCurrentRevision() {
        return currentRevision;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("isDefault", isDefault)
                .toString();
    }
}
