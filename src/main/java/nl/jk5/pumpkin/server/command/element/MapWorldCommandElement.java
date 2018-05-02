package nl.jk5.pumpkin.server.command.element;

import com.google.common.collect.ImmutableList;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NonnullByDefault
public class MapWorldCommandElement extends CommandElement {

    private final Pumpkin pumpkin;

    public MapWorldCommandElement(Pumpkin pumpkin, @Nullable Text key) {
        super(key);
        this.pumpkin = pumpkin;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String name = args.next();
        Optional<World> world;
        try{
            UUID uuid = UUID.fromString(name);
            world = this.pumpkin.game.getServer().getWorld(uuid);
            if(world.isPresent() && this.pumpkin.getMapRegistry().getMapWorld(world.get()).isPresent()){
                Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(world.get());
                return mapWorld.orElse(null);
            }
        }catch(IllegalArgumentException ignored){}
        world = this.pumpkin.game.getServer().getWorld(name);
        if(!world.isPresent() || !this.pumpkin.getMapRegistry().getMapWorld(world.get()).isPresent()){
            throw args.createError(Text.of("No world found with name ", name));
        }
        Optional<MapWorld> mapWorld = this.pumpkin.getMapRegistry().getMapWorld(world.get());
        return mapWorld.orElse(null);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final String prefix = args.nextIfPresent().orElse("");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for(World world : this.pumpkin.game.getServer().getWorlds()){
            String name = world.getName();
            if(name.startsWith(prefix)){
                builder.add(world.getName());
            }
            String uuid = world.getUniqueId().toString();
            if(uuid.startsWith(prefix)){
                builder.add(world.getUniqueId().toString());
            }
        }
        return builder.build();
    }
}
