package nl.jk5.pumpkin.server;

import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.authentication.PumpkinBanService;
import nl.jk5.pumpkin.server.command.element.MappackCommandElement;
import nl.jk5.pumpkin.server.map.MapEventListener;
import nl.jk5.pumpkin.server.map.MapRegistry;
import nl.jk5.pumpkin.server.mappack.MappackRegistry;
import nl.jk5.pumpkin.server.player.PlayerRegistry;
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
import org.spongepowered.api.service.ban.BanService;
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
import java.util.UUID;

@Plugin(id = "pumpkin", name = "Pumpkin")
public class Pumpkin {

    private static final UUID SERVER_ID = UUID.randomUUID();

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
    private PlayerRegistry playerRegistry;

    private PumpkinBanService banService;

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

        this.banService = new PumpkinBanService(this);

        this.game.getServiceManager().setProvider(this, BanService.class, this.banService);

        this.tableManager = new SqlTableManager(this, dbConn);
        this.tableManager.connect();
        this.tableManager.setupTables();

        this.mappackRegistry = new MappackRegistry(this);
        this.mapRegistry = new MapRegistry(this);
        this.playerRegistry = new PlayerRegistry(this);

        this.game.getRegistry().register(WorldGeneratorModifier.class, new VoidWorldGeneratorModifier());
        this.game.getEventManager().registerListeners(this, this.mapRegistry);
        this.game.getEventManager().registerListeners(this, new MapEventListener(this));
        this.game.getEventManager().registerListeners(this, this.playerRegistry);
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

        CommandSpec teamJoinCommand = CommandSpec.builder()
                .description(Text.of("Add a player to a team"))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.string(Text.of("team")))
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        return CommandResult.success();
                    }
                    Player player = (Player) src;
                    Optional<MapWorld> mapWorld = mapRegistry.getMapWorld(player.getWorld());
                    if(!mapWorld.isPresent()){
                        src.sendMessage(Text.of(TextColors.RED, "You are not in a valid pumpkin world"));
                        return CommandResult.empty();
                    }
                    Optional<Team> team = mapWorld.get().getMap().teamByName(args.<String>getOne("team").get());
                    if(!team.isPresent()){
                        src.sendMessage(Text.of(TextColors.RED, "That team does not exist"));
                        return CommandResult.empty();
                    }
                    Player teamPlayer = args.<Player>getOne("player").get();
                    mapWorld.get().getMap().addPlayerToTeam(teamPlayer, team.get());
                    //src.sendMessage(Text.of(TextColors.GREEN, "Player " + teamPlayer.getName() + " added to team " + team.get().getName()));
                    return CommandResult.success();
                }).build();

        CommandSpec teamRemoveCommand = CommandSpec.builder()
                .description(Text.of("Remove a player from a team"))
                .arguments(GenericArguments.player(Text.of("player")))
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        return CommandResult.success();
                    }
                    Player player = (Player) src;
                    Optional<MapWorld> mapWorld = mapRegistry.getMapWorld(player.getWorld());
                    if(!mapWorld.isPresent()){
                        src.sendMessage(Text.of(TextColors.RED, "You are not in a valid pumpkin world"));
                        return CommandResult.empty();
                    }
                    Player teamPlayer = args.<Player>getOne("player").get();
                    Optional<Team> team = mapWorld.get().getMap().getPlayerTeam(teamPlayer);
                    if(!team.isPresent()){
                        src.sendMessage(Text.of(TextColors.RED, "That player is not in a team"));
                        return CommandResult.empty();
                    }
                    mapWorld.get().getMap().removePlayerFromTeam(teamPlayer);
                    //src.sendMessage(Text.of(TextColors.GREEN, "Player " + teamPlayer.getName() + " has been removed from team " + team.get().getName()));
                    return CommandResult.success();
                }).build();

        CommandSpec teamCommand = CommandSpec.builder()
                .description(Text.of("Team commands"))
                .child(teamJoinCommand, "join")
                .child(teamRemoveCommand, "remove")
                .build();

        game.getCommandManager().register(this, mappackCommand, "mappack");
        //game.getCommandManager().register(this, mappackLoadCommand, "loadmappack");
        game.getCommandManager().register(this, gotoCommand, "goto");
        game.getCommandManager().register(this, teamCommand, "team");
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
}
