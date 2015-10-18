package nl.jk5.pumpkin.sponge.commands.executors;

import com.google.common.collect.ImmutableMap;
import nl.jk5.pumpkin.sponge.commands.elements.EntityCommandElement;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@NonnullByDefault
public final class GameModeExecutor implements CommandExecutor {

    private static final Map<String, GameMode> gameModes = ImmutableMap.<String, GameMode>builder()
            .put("survival", GameModes.SURVIVAL)
            .put("creative", GameModes.CREATIVE)
            .put("adventure", GameModes.ADVENTURE)
            .put("spectator", GameModes.SPECTATOR)
            .build();

    private GameModeExecutor() {}

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(!args.hasAny("gamemode") && !args.hasAny("player")){
            if(!(src instanceof Player)){
                src.sendMessage(Texts.of(TextColors.RED, "No arguments specified and you are not a player. To change the gamemode of a player specify a gamemode and the player's name"));
                return CommandResult.empty();
            }
            Player player = ((Player) src);
            if(!player.hasPermission("pumpkin.command.gamemode.self")){
                player.sendMessage(Texts.of(TextColors.RED, "You may not change your own gamemode"));
                return CommandResult.empty();
            }
            GameMode mode = player.getGameModeData().get(Keys.GAME_MODE).get();
            if(mode == GameModes.CREATIVE){
                mode = GameModes.SURVIVAL;
            }else{
                mode = GameModes.CREATIVE;
            }
            GameModeData newData = player.getGameModeData().set(Keys.GAME_MODE, mode);
            DataTransactionResult result = player.offer(newData);
            switch(result.getType()){
                case SUCCESS:
                    player.sendMessage(Texts.of(TextColors.GREEN, "Your gamemode has been changed to ", mode.getTranslation()));
                    return CommandResult.success();
                case CANCELLED:
                    player.sendMessage(Texts.of(TextColors.GOLD, "Your gamemode was not changed to ", mode.getTranslation(), " because it was cancelled"));
                    return CommandResult.empty();
                case FAILURE:
                case ERROR:
                case UNDEFINED:
                default:
                    player.sendMessage(Texts.of(TextColors.RED, "An error has occurred while changing your gamemode to ", mode.getTranslation()));
                    return CommandResult.empty();
            }
        }else if(!args.hasAny("player")){
            GameMode mode = args.<GameMode>getOne("gamemode").get();
            if(!(src instanceof Player)){
                src.sendMessage(Texts.of(TextColors.RED, "No player target specified and you are not a player. To change the gamemode of a player specify the player's name"));
                return CommandResult.empty();
            }
            Player player = ((Player) src);
            if(!player.hasPermission("pumpkin.command.gamemode.self")){
                player.sendMessage(Texts.of(TextColors.RED, "You may not change your own gamemode"));
                return CommandResult.empty();
            }
            GameModeData newData = player.getGameModeData().set(Keys.GAME_MODE, mode);
            DataTransactionResult result = player.offer(newData);
            switch(result.getType()){
                case SUCCESS:
                    player.sendMessage(Texts.of(TextColors.GREEN, "Your gamemode has been changed to ", mode.getTranslation()));
                    return CommandResult.success();
                case CANCELLED:
                    player.sendMessage(Texts.of(TextColors.GOLD, "Your gamemode was not changed to ", mode.getTranslation(), " because it was cancelled"));
                    return CommandResult.empty();
                case FAILURE:
                case ERROR:
                case UNDEFINED:
                default:
                    player.sendMessage(Texts.of(TextColors.RED, "An error has occurred while changing your gamemode to ", mode.getTranslation()));
                    return CommandResult.empty();
            }
        }else{
            Collection<Entity> targets = args.<Entity>getAll("player");
            GameMode mode = args.<GameMode>getOne("gamemode").get();
            Boolean selfAllowed = null;
            Boolean otherAllowed = null;
            List<Player> success = new ArrayList<>();
            List<Player> rejected = new ArrayList<>();
            List<Player> errorred = new ArrayList<>();
            for (Entity ent : targets) {
                Player target;
                if(ent instanceof Player){
                    target = ((Player) ent);
                }else{
                    continue;
                }
                if(target == src){
                    if(selfAllowed == null){
                        selfAllowed = src.hasPermission("pumpkin.command.gamemode.self");
                    }
                    if(!selfAllowed){
                        rejected.add(target);
                        continue;
                    }
                }else{
                    if(otherAllowed == null){
                        otherAllowed = src.hasPermission("pumpkin.command.gamemode.other");
                    }
                    if(!otherAllowed){
                        rejected.add(target);
                        continue;
                    }
                }
                GameModeData newData = target.getGameModeData().set(Keys.GAME_MODE, mode);
                DataTransactionResult result = target.offer(newData);
                switch(result.getType()){
                    case SUCCESS:
                        success.add(target);
                        target.sendMessage(Texts.of(TextColors.GREEN, "Your gamemode has been changed to ", mode.getTranslation()));
                        break;
                    case CANCELLED:
                        rejected.add(target);
                        break;
                    case FAILURE:
                    case ERROR:
                    case UNDEFINED:
                    default:
                        errorred.add(target);
                        break;
                }
            }
            if(otherAllowed == null && selfAllowed == null){
                src.sendMessage(Texts.of(TextColors.GOLD, "No players matched"));
                return CommandResult.empty();
            }else if(otherAllowed == null){
                return CommandResult.builder().successCount(1).affectedEntities(1).build();
            }else{
                TextBuilder succeededTooltip = Texts.builder();
                success.stream().map(Player::getName).map(Texts::of).forEach(succeededTooltip::append);

                TextBuilder builder = Texts.builder("Gamemode result:").color(TextColors.GOLD).append(Texts.of(" "));
                if(!success.isEmpty()) builder.append(Texts.builder(success.size() + " succeeded ").color(TextColors.GREEN).onHover(TextActions.showText(succeededTooltip.build())).build());
                if(!rejected.isEmpty()) builder.append(Texts.of(TextColors.GOLD, rejected.size() + " rejected "));
                if(!errorred.isEmpty()) builder.append(Texts.of(TextColors.RED, errorred.size() + " errorred"));

                src.sendMessage(builder.build());

                return CommandResult.builder().successCount(success.size()).affectedEntities(success.size()).build();
            }
        }
    }

    public static CommandCallable createRegistration(Game game) {
        return CommandSpec.builder()
                .description(Texts.of("Change your gamemode"))
                .permission("pumpkin.command.gamemode.use")
                .arguments(GenericArguments.seq(
                        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.choices(Texts.of("gamemode"), gameModes, true))),
                        //GenericArguments.optional(GenericArguments.player(Texts.of("player"), game))
                        GenericArguments.optional(new EntityCommandElement(Texts.of("player"), game))
                ))
                .executor(new GameModeExecutor())
                .build();
    }
}
