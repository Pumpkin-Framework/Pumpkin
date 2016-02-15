package nl.jk5.pumpkin.server;

import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.command.element.MappackCommandElement;
import nl.jk5.pumpkin.server.map.MapEventListener;
import nl.jk5.pumpkin.server.map.MapRegistry;
import nl.jk5.pumpkin.server.mappack.MappackRegistry;
import nl.jk5.pumpkin.server.services.PumpkinServiceManger;
import nl.jk5.pumpkin.server.sql.SqlTableManager;
import nl.jk5.pumpkin.server.utils.WorldUtils;
import nl.jk5.pumpkin.server.world.gen.empty.VoidWorldGeneratorModifier;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

@Plugin(id = "pumpkin", name = "Pumpkin")
public class Pumpkin {

    @Inject public Logger logger;
    @Inject public Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private CommentedConfigurationNode config;

    private PumpkinServiceManger serviceManager;
    private SqlTableManager tableManager;
    private MappackRegistry mappackRegistry;
    private MapRegistry mapRegistry;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) throws IOException {
        config = configManager.load();

        if(config.getNode("database", "connection-string").isVirtual()){
            config.getNode("database", "connection-string")
                    .setComment("The JDBC connection string for the database")
                    .setValue("jdbc:postgresql://HOST/DATABASE?user=USER&password=PASSWORD");
        }

        if(config.getNode("lobby-mappack").isVirtual()){
            config.getNode("lobby-mappack")
                    .setComment("The mappack to use for the lobby. The lobby is the map where players will enter if they join the server for the first time")
                    .setValue(-1);
        }

        String dbConn = config.getNode("database", "connection-string")
                .setComment("The JDBC connection string for the database")
                .getString();

        try{
            configManager.save(config);
        }catch(IOException e){
            Log.error("Could not save config", e);
        }

        this.serviceManager = new PumpkinServiceManger(game);

        this.tableManager = new SqlTableManager(this, dbConn);
        this.tableManager.connect();
        this.tableManager.setupTables();

        this.mappackRegistry = new MappackRegistry(this);
        this.mapRegistry = new MapRegistry(this);

        this.game.getRegistry().register(WorldGeneratorModifier.class, new VoidWorldGeneratorModifier());
        this.game.getEventManager().registerListeners(this, this.mapRegistry);
        this.game.getEventManager().registerListeners(this, new MapEventListener(this));
    }

    @Listener
    public void onInit(GameInitializationEvent event){
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            logger.error("Was not able to load SQL drivers");
        }
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event) throws SQLException {
        WorldUtils.unregisterDimension(-1);
        WorldUtils.unregisterDimension(1);
        WorldUtils.releaseDimensionId(-1);
        WorldUtils.releaseDimensionId(1);

        //To change the generator type, see MixinMinecraftServer.loadAllWorlds (SpongeCommon)
        //The worldInfo var is loaded from the level.dat file. To change it in the code we need to patch that loading code
        //Void generator setting: 3;minecraft:air;127;decoration
        //Lava generator setting: 3;minecraft:bedrock,16*minecraft:lava;8;

        CommandSpec mappackLoadCommand = CommandSpec.builder()
                .description(Text.of("Load a mappack"))
                .arguments(new MappackCommandElement(this, Text.of("mappack")))
                //.arguments(GenericArguments.choices(Text.of("mappack"), this.mappackRegistry.getAllMappacks().stream().collect(Collectors.toMap(Mappack::getName, Function.identity()))))
                .executor((src, args) -> {
                    Optional<Mappack> mappack = args.getOne("mappack");
                    if(mappack.isPresent()){
                        this.mapRegistry.load(mappack.get());
                    }
                    return CommandResult.success();
                })
                .build();

        CommandSpec mappackCommand = CommandSpec.builder()
                .description(Text.of("Mappack commands"))
                .child(mappackLoadCommand, "load")
                .build();

        CommandSpec gotoCommand = CommandSpec.builder()
                .description(Text.of("Go to a world"))
                .arguments(GenericArguments.string(Text.of("world name")))
                .executor((src, args) -> {
                    String name = args.<String>getOne("world name").get();
                    if(!(src instanceof Player)){
                        return CommandResult.success();
                    }
                    Player player = (Player) src;
                    World world = game.getServer().getWorld(name).get();
                    Optional<MapWorld> mapWorld = mapRegistry.getMapWorld(world);
                    if(!mapWorld.isPresent()){
                        src.sendMessage(Text.of(TextColors.RED, "That is not a valid world"));
                        return CommandResult.empty();
                    }
                    PlayerLocation spawn = mapWorld.get().getConfig().getSpawnpoint();
                    player.setLocationAndRotation(new Location<>(world, spawn.getX(), spawn.getY(), spawn.getZ()), new Vector3d(spawn.getPitch(), spawn.getYaw(), 0));
                    return CommandResult.success();
                })
                .build();

        game.getCommandManager().register(this, mappackCommand, "mappack");
        game.getCommandManager().register(this, mappackLoadCommand, "loadmappack");
        game.getCommandManager().register(this, gotoCommand, "goto");
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        int lobbyId = config.getNode("lobby-mappack").getInt();
        Optional<Mappack> lobby = this.getMappackRegistry().byId(lobbyId);
        if(!lobby.isPresent()){
            Log.error("Mappack with id " + lobbyId + " not found. Could not load lobby");
            throw new RuntimeException("Mappack with id " + lobbyId + " not found. Could not load lobby");
        }

        Optional<Map> lobbyMap = this.mapRegistry.load(lobby.get());
        if(!lobbyMap.isPresent()){
            Log.error("Could not load lobby map");
            throw new RuntimeException("Could not load lobby map");
        }

        this.mapRegistry.setLobby(lobbyMap.get());
    }

    //@Listener
    //public void onCreateWorld(ConstructWorldEvent event){
    //    //event.getWorldProperties().setGeneratorType(GeneratorTypes.FLAT);
    //    //event.getWorldProperties().setGeneratorType(PumpkinGeneratorTypes.VOID);
    //    //event.getWorldProperties().getGeneratorType().getGeneratorSettings().set(DataQuery.of("customSettings"), "3;minecraft:bedrock,16*minecraft:lava;8;");
    //
    //    Log.info(event.getWorldProperties().getGeneratorSettings().toString());
    //    Log.info(event.getWorldProperties().getGeneratorModifiers().toString());
    //    Log.info(event.getWorldProperties().getGeneratorType().toString());
    //    Log.info(event.getWorldProperties().getGeneratorType().getGeneratorSettings().getString(DataQuery.of("customSettings")).orElse(null));
    //}

    //@Listener
    //public void onLoadWorld(LoadWorldEvent event){
    //    //event.getTargetWorld().getProperties().setGeneratorType(GeneratorTypes.FLAT);
    //}

    //@Listener
    /*public void onServerStarting(GameStartingServerEvent event){
        WorldCreationSettings creationSettings = WorldCreationSettings.builder()
                .dimension(DimensionTypes.NETHER)
                .generator(GeneratorTypes.FLAT)
                .name("lobby")
                .enabled(true)
                .loadsOnStartup(true)
                //.generator()
                .build();

        Optional<WorldProperties> worldProperties = game.getServer().createWorldProperties(creationSettings);
        if(!worldProperties.isPresent()){
            Log.warn("Could not create world");
            return;
        }

        //Optional<World> world = game.getServer().loadWorld(worldProperties.get());
        //if(!world.isPresent()){
        //    Log.warn("Could not create world");
        //    return;
        //}
    }*/

    public PumpkinServiceManger getServiceManager() {
        return serviceManager;
    }

    public Game getGame() {
        return game;
    }

    public SqlTableManager getTableManager() {
        return tableManager;
    }

    public MappackRegistry getMappackRegistry() {
        return mappackRegistry;
    }

    public MapRegistry getMapRegistry() {
        return mapRegistry;
    }
}
