package nl.jk5.pumpkin.server.command.executor;

import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class MappackLoadCommandExecutor implements CommandExecutor {

    private final Pumpkin pumpkin;

    public MappackLoadCommandExecutor(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Optional<Mappack> mappack = args.getOne("mappack");
        if(!mappack.isPresent()){
            return CommandResult.empty();
        }

        pumpkin.getMapRegistry().load(mappack.get()).whenComplete((map, e) -> {
            if(e != null){
                Log.error("Could not load map " + mappack.get().getName() + " (" + mappack.get().getId() + ")", e);
                src.sendMessage(Text.of(TextColors.RED, "Failed to load map ", TextColors.GOLD, mappack.get().getName()));
            }else{
                Text.Builder builder = Text.builder();
                builder.append(Text.of(TextColors.GREEN, "Successfully loaded mappack ", TextColors.GOLD, mappack.get().getName(), TextColors.GREEN, ". Click "));
                builder.append(Text.of(TextColors.GOLD, TextActions.runCommand("/goto " + map.getDefaultWorld().getWorld().getUniqueId().toString()), TextActions.showText(Text.of("/goto " + map.getDefaultWorld().getWorld().getUniqueId().toString())), "here"));
                builder.append(Text.of(TextColors.GREEN, " to teleport to it"));
                src.sendMessage(builder.build());
            }
        });
        return CommandResult.success();
    }
}
