package nl.jk5.pumpkin.server;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.authentication.PumpkinBanService;
import nl.jk5.pumpkin.server.command.Commands;
import nl.jk5.pumpkin.server.map.MapEventListener;
import nl.jk5.pumpkin.server.map.MapRegistry;
import nl.jk5.pumpkin.server.mappack.MappackRegistry;
import nl.jk5.pumpkin.server.player.PlayerRegistry;
import nl.jk5.pumpkin.server.services.PumpkinServiceManger;
import nl.jk5.pumpkin.server.settings.PumpkinSettings;
import nl.jk5.pumpkin.server.sql.SqlTableManager;
import nl.jk5.pumpkin.server.utils.WorldUtils;
import nl.jk5.pumpkin.server.world.gen.empty.VoidWorldGeneratorModifier;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Plugin(id = "nl.jk5.pumpkin")
public class Pumpkin {

    private static final UUID SERVER_ID = UUID.randomUUID();
    private static Pumpkin INSTANCE;

    @Inject public Logger logger;
    @Inject public Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private PumpkinServiceManger serviceManager;
    private SqlTableManager tableManager;
    private MappackRegistry mappackRegistry;
    private MapRegistry mapRegistry;
    private PumpkinSettings settings;

    private MapEventListener mapEventListener;

    public Pumpkin() {
        INSTANCE = this;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) throws IOException {
        this.settings = new PumpkinSettings(this.configManager);

        this.serviceManager = new PumpkinServiceManger(game);

        PumpkinBanService banService = new PumpkinBanService(this);

        this.game.getServiceManager().setProvider(this, BanService.class, banService);

        this.tableManager = new SqlTableManager(this, this.settings.getDatabaseConnectionString());
        this.tableManager.connect();
        this.tableManager.setupTables();

        this.mappackRegistry = new MappackRegistry(this);
        this.mapRegistry = new MapRegistry(this);
        this.mapEventListener = new MapEventListener(this);
        PlayerRegistry playerRegistry = new PlayerRegistry(this);

        this.game.getRegistry().register(WorldGeneratorModifier.class, new VoidWorldGeneratorModifier());
        this.game.getEventManager().registerListeners(this, this.mapRegistry);
        this.game.getEventManager().registerListeners(this, this.mapEventListener);
        this.game.getEventManager().registerListeners(this, playerRegistry);
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
        /*
         * Not really technically needed to have these worlds unloaded, it's just for performance improvements,
         * as the default worlds are always loaded.
         */
        WorldUtils.unregisterDimension(-1);
        WorldUtils.unregisterDimension(1);
        WorldUtils.releaseDimensionId(-1);
        WorldUtils.releaseDimensionId(1);

        //To change the generator type, see MixinMinecraftServer.loadAllWorlds (SpongeCommon)
        //The worldInfo var is loaded from the level.dat file. To change it in the code we need to patch that loading code
        //Void generator setting: 3;minecraft:air;127;decoration
        //Lava generator setting: 3;minecraft:bedrock,16*minecraft:lava;8;

        Commands.register(this);
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        int lobbyId = this.settings.getLobbyMappack();
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

    public static UUID getServerId(){
        return SERVER_ID;
    }

    public static Pumpkin instance(){
        return INSTANCE;
    }

    public PumpkinSettings getSettings() {
        return settings;
    }

    public MapEventListener getMapEventListener() {
        return mapEventListener;
    }
}
