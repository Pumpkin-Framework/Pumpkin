package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.server.scripting.AbstractValue;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Context;

public class TextCreateValue implements AbstractValue {

    @Override
    public Object[] call(Context context, Arguments args) {
        return new Object[]{new TextValue(MapComponent.convertText(args, 0))};
    }
}
