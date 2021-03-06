package nl.jk5.pumpkin.server.command;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.mappack.game.GameStartResult;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.command.element.MapWorldCommandElement;
import nl.jk5.pumpkin.server.command.element.MappackCommandElement;
import nl.jk5.pumpkin.server.command.executor.GotoCommandExecutor;
import nl.jk5.pumpkin.server.command.executor.MappackLoadCommandExecutor;
import nl.jk5.pumpkin.server.map.stat.StatEmitter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class Commands {

    private Commands(){
        throw new UnsupportedOperationException();
    }

    public static void register(Pumpkin pumpkin){
        CommandSpec mappackLoadCommand = CommandSpec.builder()
                .description(Text.of("Load a mappack"))
                .arguments(new MappackCommandElement(pumpkin, Text.of("mappack")))
                .executor(new MappackLoadCommandExecutor(pumpkin))
                .build();

        CommandSpec mappackCommand = CommandSpec.builder()
                .description(Text.of("Mappack commands"))
                .child(mappackLoadCommand, "load")
                .build();

        CommandSpec gotoCommand = CommandSpec.builder()
                .description(Text.of("Go to a world"))
                .arguments(new MapWorldCommandElement(pumpkin, Text.of("world")))
                .executor(new GotoCommandExecutor())
                .build();

        CommandSpec teamJoinCommand = CommandSpec.builder()
                .description(Text.of("Add a player to a team"))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.string(Text.of("team")))
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        return CommandResult.success();
                    }
                    Player player = (Player) src;
                    Optional<MapWorld> mapWorld = pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                    Optional<MapWorld> mapWorld = pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                    Optional<MapWorld> mapWorld = pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                    Optional<MapWorld> mapWorld = pumpkin.getMapRegistry().getMapWorld(player.getWorld());
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
                    World world = pumpkin.game.getServer().getWorld("world").get();
                    Player player = (Player) src;
                    player.setLocation(new Location<>(world, 0, 64, 0));
                    return CommandResult.success();
                }).build();

        CommandSpec statEmitterCommand = CommandSpec.builder()
                .description(Text.of("Stat emitter commands"))
                .child(statEmitterCreateCommand, "create")
                .build();

        pumpkin.game.getCommandManager().register(pumpkin, mappackCommand, "mappack");
        pumpkin.game.getCommandManager().register(pumpkin, gotoCommand, "goto");
        pumpkin.game.getCommandManager().register(pumpkin, teamCommand, "team");
        pumpkin.game.getCommandManager().register(pumpkin, gameCommand, "game");
        pumpkin.game.getCommandManager().register(pumpkin, statEmitterCommand, "statemitter");
        pumpkin.game.getCommandManager().register(pumpkin, tpworldCommand, "tpworld");
    }
}
