package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.api.mappack.game.stat.StatType;
import nl.jk5.pumpkin.server.map.stat.type.ManualStatType;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;

public class StatValue extends SimpleValue<StatType> {

    public StatValue(StatType value) {
        super(value);
    }

    @Callback
    public Object[] enable(Context ctx, Arguments args){
        if(getValue() instanceof ManualStatType){
            ManualStatType type = (ManualStatType) getValue();
            type.enable();
        }
        return new Object[0];
    }

    @Callback
    public Object[] disable(Context ctx, Arguments args){
        if(getValue() instanceof ManualStatType){
            ManualStatType type = (ManualStatType) getValue();
            type.disable();
        }
        return new Object[0];
    }

    @Callback
    public Object[] trigger(Context ctx, Arguments args){
        if(getValue() instanceof ManualStatType){
            ManualStatType type = (ManualStatType) getValue();
            type.trigger();
        }
        return new Object[0];
    }
}
