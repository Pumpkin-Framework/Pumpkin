package nl.jk5.pumpkin.server;

import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.mappack.game.GameStartResult;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import nl.jk5.pumpkin.server.authentication.PumpkinBanService;
import nl.jk5.pumpkin.server.command.element.MappackCommandElement;
import nl.jk5.pumpkin.server.map.MapEventListener;
import nl.jk5.pumpkin.server.map.MapRegistry;
import nl.jk5.pumpkin.server.map.stat.StatEmitter;
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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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

        CommandSpec gameStartCommand = CommandSpec.builder()
                .description(Text.of("Start the game in this map"))
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
                    Map map = mapWorld.get().getMap();
                    map.getGame().ifPresent(game -> {
                        GameStartResult result = game.start();
                        src.sendMessage(result.message());
                    });
                    return CommandResult.success();
                }).build();

        CommandSpec gameCommand = CommandSpec.builder()
                .description(Text.of("Game commands"))
                .child(gameStartCommand, "start")
                .build();

        CommandSpec statEmitterCreateCommand = CommandSpec.builder()
                .description(Text.of("Create a stat emitter"))
                .arguments(GenericArguments.integer(Text.of("x")), GenericArguments.integer(Text.of("y")), GenericArguments.integer(Text.of("z")))
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
                    Map map = mapWorld.get().getMap();
                    int x = args.<Integer>getOne("x").get();
                    int y = args.<Integer>getOne("y").get();
                    int z = args.<Integer>getOne("z").get();
                    Optional<StatEmitter> emitter = StatEmitter.create(mapWorld.get(), new Location<>(mapWorld.get().getWorld(), x, y, z));
                    if(emitter.isPresent()){
                        map.getStatManager().addStatEmitter(emitter.get());
                    }
                    return CommandResult.success();
                }).build();

        CommandSpec tpworldCommand = CommandSpec.builder()
                .description(Text.of("Tp to a world"))
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        return CommandResult.success();
                    }
                    World world = game.getServer().getWorld("world").get();
                    Player player = (Player) src;
                    player.setLocation(new Location<>(world, 0, 64, 0));
                    return CommandResult.success();
                }).build();

        CommandSpec statEmitterCommand = CommandSpec.builder()
                .description(Text.of("Stat emitter commands"))
                .child(statEmitterCreateCommand, "create")
                .build();

        game.getCommandManager().register(this, mappackCommand, "mappack");
        game.getCommandManager().register(this, gotoCommand, "goto");
        game.getCommandManager().register(this, teamCommand, "team");
        game.getCommandManager().register(this, gameCommand, "game");
        game.getCommandManager().register(this, statEmitterCommand, "statemitter");
        game.getCommandManager().register(this, tpworldCommand, "tpworld");
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
