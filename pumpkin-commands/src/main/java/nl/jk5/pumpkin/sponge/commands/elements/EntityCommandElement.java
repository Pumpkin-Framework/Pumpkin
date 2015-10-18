package nl.jk5.pumpkin.sponge.commands.elements;

import com.google.common.collect.Iterables;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Temporary until https://github.com/SpongePowered/SpongeAPI/pull/780 gets pulled
 */
@NonnullByDefault
public class EntityCommandElement extends SelectorCommandElement {

    private final Game game;

    public EntityCommandElement(@Nullable Text key, Game game){
        super(key);
        this.game = game;
    }

    @Override
    protected Iterable<String> getChoices(CommandSource source) {
        Set<Iterable<Entity>> worldEntities = this.game.getServer().getWorlds().stream().map(World::getEntities).collect(Collectors.toSet());
        return Iterables.transform(Iterables.concat(worldEntities), input -> {
            if(input == null){
                return null;
            }
            if(input instanceof Player){
                return ((Player) input).getName();
            }
            return input.getUniqueId().toString();
        });
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        UUID uuid = UUID.fromString(choice);
        for(World world : this.game.getServer().getWorlds()){
            Optional<Entity> ret = world.getEntity(uuid);
            if(ret.isPresent()){
                return ret.get();
            }
        }
        throw new IllegalArgumentException("Input value " + choice + " was not an entity");
    }
}
