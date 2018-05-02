package nl.jk5.pumpkin.server.command.executor;

import com.flowpowered.math.vector.Vector3d;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;

import java.util.Optional;

@NonnullByDefault
public class GotoCommandExecutor implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<MapWorld> mapWorld = args.getOne("world");
        if(!(src instanceof Player)){
            return CommandResult.success();
        }
        Player player = (Player) src;

        PlayerLocation spawn = mapWorld.get().getConfig().getSpawnpoint();
        player.setLocationAndRotation(new Location<>(mapWorld.get().getWorld(), spawn.getX(), spawn.getY(), spawn.getZ()), new Vector3d(spawn.getPitch(), spawn.getYaw(), 0));
        return CommandResult.success();
    }
}
