package nl.jk5.pumpkin.sponge.commands.elements;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selectors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.ArgumentParseException;
import org.spongepowered.api.util.command.args.CommandArgs;
import org.spongepowered.api.util.command.args.PatternMatchingCommandElement;

import javax.annotation.Nullable;

/**
 * Temporary until https://github.com/SpongePowered/SpongeAPI/pull/780 gets pulled
 */
@NonnullByDefault
public abstract class SelectorCommandElement extends PatternMatchingCommandElement {

    public SelectorCommandElement(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Object state = args.getState();
        try{
            return super.parseValue(source, args);
        }catch(ArgumentParseException e){
            args.setState(state);
            try{
                return Selectors.parse(args.next()).resolve(source);
            }catch(RuntimeException e1){
                throw e;
            }
        }
    }

    //TODO: implement complete
}
