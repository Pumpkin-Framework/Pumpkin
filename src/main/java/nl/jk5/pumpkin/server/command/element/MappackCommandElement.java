package nl.jk5.pumpkin.server.command.element;

import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

@NonnullByDefault
public class MappackCommandElement extends CommandElement {

    private final Pumpkin pumpkin;

    public MappackCommandElement(Pumpkin pumpkin, @Nullable Text key) {
        super(key);
        this.pumpkin = pumpkin;
    }

    @Override
    public Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String input = args.next();
        Optional<Mappack> mappack = this.pumpkin.getMappackRegistry().byName(input);
        if(mappack.isPresent()){
            return mappack.get();
        }else{
            throw args.createError(t("Mappack %s not found", input));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final String prefix = args.nextIfPresent().orElse("");
        return this.pumpkin.getMappackRegistry().getAllMappacks().stream().map(Mappack::getName).filter(new StartsWithPredicate(prefix)).collect(GuavaCollectors.toImmutableList());
    }
}
