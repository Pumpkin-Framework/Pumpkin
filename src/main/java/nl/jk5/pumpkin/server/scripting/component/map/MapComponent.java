package nl.jk5.pumpkin.server.scripting.component.map;

import com.google.common.collect.ImmutableMap;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import nl.jk5.pumpkin.server.scripting.component.AnnotatedComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.LinkedList;
import java.util.List;

@AnnotatedComponent.Type("map")
public class MapComponent extends AnnotatedComponent {

    private static final ImmutableMap<String, TextColorValue> colors = ImmutableMap.<String, TextColorValue>builder()
            .put("aqua", new TextColorValue(TextColors.AQUA))
            .put("black", new TextColorValue(TextColors.BLACK))
            .put("blue", new TextColorValue(TextColors.BLUE))
            .put("darkAqua", new TextColorValue(TextColors.DARK_AQUA))
            .put("darkBlue", new TextColorValue(TextColors.DARK_BLUE))
            .put("darkGray", new TextColorValue(TextColors.DARK_GRAY))
            .put("darkGreen", new TextColorValue(TextColors.DARK_GREEN))
            .put("darkPurple", new TextColorValue(TextColors.DARK_PURPLE))
            .put("darkRed", new TextColorValue(TextColors.DARK_RED))
            .put("gold", new TextColorValue(TextColors.GOLD))
            .put("gray", new TextColorValue(TextColors.GRAY))
            .put("green", new TextColorValue(TextColors.GREEN))
            .put("lightPurple", new TextColorValue(TextColors.LIGHT_PURPLE))
            .put("red", new TextColorValue(TextColors.RED))
            .put("reset", new TextColorValue(TextColors.RESET))
            .put("white", new TextColorValue(TextColors.WHITE))
            .put("yellow", new TextColorValue(TextColors.YELLOW))
            .build();

    private static final ImmutableMap<String, TextStyleValue> styles = ImmutableMap.<String, TextStyleValue>builder()
            .put("bold", new TextStyleValue(TextStyles.BOLD))
            .put("italic", new TextStyleValue(TextStyles.ITALIC))
            .put("obfuscated", new TextStyleValue(TextStyles.OBFUSCATED))
            .put("reset", new TextStyleValue(TextStyles.RESET))
            .put("striketrough", new TextStyleValue(TextStyles.STRIKETHROUGH))
            .put("underline", new TextStyleValue(TextStyles.UNDERLINE))
            .build();

    private final Map map;

    public MapComponent(Map map) {
        this.map = map;
    }

    @Override
    public String address() {
        return "/dev/map";
    }

    @Callback(direct = true)
    public Object[] getMap(Context ctx, Arguments args){
        return new Object[]{map};
    }

    @Callback(direct = true)
    public Object[] getTextColors(Context ctx, Arguments args){
        return new Object[]{colors};
    }

    @Callback(direct = true)
    public Object[] getTextStyles(Context ctx, Arguments args){
        return new Object[]{styles};
    }

    @Callback(direct = true)
    public Object[] createText(Context ctx, Arguments args){
        return new Object[]{new TextCreateValue()};
    }

    public static Text convertText(Arguments args, int index){
        List<Object> elements = new LinkedList<>();
        for(int i = index; i < args.count(); i++){
            Object e = args.checkAny(i);
            if(e instanceof SimpleValue){
                elements.add(((SimpleValue) e).getValue());
            }else if(e instanceof byte[]){
                elements.add(new String((byte[]) e));
            }else{
                elements.add(e);
            }
        }
        return Text.of(elements.toArray(new Object[elements.size()]));
    }

    public static Text getText(Arguments args, int index) {
        Object e = args.checkAny(index);
        if(!(e instanceof SimpleValue) || !(((SimpleValue) e).getValue() instanceof Text)){
            throw new IllegalArgumentException("#" + index + ": expected message object");
        }
        return ((Text) ((SimpleValue) e).getValue());
    }
}
