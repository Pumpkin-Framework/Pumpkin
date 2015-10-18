package nl.jk5.pumpkin.sponge.commands.executors;

import nl.jk5.pumpkin.sponge.commands.elements.EntityCommandElement;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
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
import java.util.List;

@NonnullByDefault
public class HealExecutor implements CommandExecutor {

    private HealExecutor() {}

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(!args.hasAny("player")){
            if(src instanceof Entity && ((Entity) src).supports(Keys.HEALTH)){
                if(!src.hasPermission("pumpkin.command.gamemode.self")){
                    src.sendMessage(Texts.of(TextColors.RED, "You may not heal yourself"));
                    return CommandResult.empty();
                }
                Entity entity = (Entity) src;
                double newHealth = entity.get(Keys.MAX_HEALTH).get();
                DataTransactionResult result = entity.offer(Keys.HEALTH, newHealth);
                switch(result.getType()){
                    case SUCCESS:
                        src.sendMessage(Texts.of(TextColors.GREEN, "You have been healed"));
                        return CommandResult.builder().successCount(1).affectedEntities(1).build();
                    case CANCELLED:
                        src.sendMessage(Texts.of(TextColors.GOLD, "You have not been healed because it has been cancelled"));
                        return CommandResult.empty();
                    case ERROR:
                    case UNDEFINED:
                    case FAILURE:
                    default:
                        src.sendMessage(Texts.of(TextColors.RED, "An error occurred while healing"));
                        return CommandResult.empty();
                }
            }else{
                src.sendMessage(Texts.of(TextColors.RED, "You can't heal yourself because you don't have health. Specify an username"));
                return CommandResult.empty();
            }
        }else{
            Boolean selfAllowed = null;
            Boolean otherAllowed = null;
            List<Entity> success = new ArrayList<>();
            List<Entity> rejected = new ArrayList<>();
            List<Entity> errorred = new ArrayList<>();
            for(Entity target : args.<Entity>getAll("player")){
                if(!target.supports(Keys.HEALTH)){
                    continue;
                }
                if(target == src){
                    if(selfAllowed == null){
                        selfAllowed = src.hasPermission("pumpkin.command.heal.self");
                    }
                    if(!selfAllowed){
                        rejected.add(target);
                        continue;
                    }
                }else{
                    if(otherAllowed == null){
                        otherAllowed = src.hasPermission("pumpkin.command.heal.other");
                    }
                    if(!otherAllowed){
                        rejected.add(target);
                        continue;
                    }
                }

                DataTransactionResult result = target.offer(Keys.HEALTH, target.get(Keys.MAX_HEALTH).get());
                switch(result.getType()){
                    case SUCCESS:
                        success.add(target);
                        if(target instanceof CommandSource) ((CommandSource) target).sendMessage(Texts.of(TextColors.GREEN, "You have been healed"));
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
                TextBuilder builder = Texts.builder("Gamemode result:").color(TextColors.GOLD).append(Texts.of(" "));
                if(!success.isEmpty()) builder.append(Texts.of(TextColors.GREEN, success.size() + " succeeded "));
                if(!rejected.isEmpty()) builder.append(Texts.of(TextColors.GOLD, rejected.size() + " rejected "));
                if(!errorred.isEmpty()) builder.append(Texts.of(TextColors.RED, errorred.size() + " errorred"));
                src.sendMessage(builder.build());
                return CommandResult.builder().successCount(success.size()).affectedEntities(success.size()).build();
            }
        }
    }

    public static CommandCallable createRegistration(Game game) {
        return CommandSpec.builder()
                .description(Texts.of("Heal a player or entity"))
                .permission("pumpkin.command.heal.use")
                .arguments(GenericArguments.optional(new EntityCommandElement(Texts.of("player"), game)))
                .executor(new HealExecutor())
                .build();
    }
}
