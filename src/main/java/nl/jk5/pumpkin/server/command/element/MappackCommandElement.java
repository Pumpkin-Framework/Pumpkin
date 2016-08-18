package nl.jk5.pumpkin.server.command.element;

import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.exception.MappackNotFoundException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        Mappack mappack = null;
        try {
            mappack = this.pumpkin.getMappackRegistry().byName(input).get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            if(e.getCause() instanceof MappackNotFoundException){
                throw args.createError(t("Mappack %s not found", input));
            }
            Log.error("Error while searching for mappack ", e.getCause());
            throw args.createError(Text.of(TextColors.RED, "Error while searching mappack ", TextColors.GOLD, e.getCause().getMessage()));
        }
        return mappack;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final String prefix = args.nextIfPresent().orElse("");
        try {
            return this.pumpkin.getMappackRegistry().getAllMappacks().get().stream().map(Mappack::getName).filter(new StartsWithPredicate(prefix)).collect(GuavaCollectors.toImmutableList());
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            Log.error("Error while autocompleting for mappack ", e.getCause());
        }
        return Collections.emptyList();
    }
}
