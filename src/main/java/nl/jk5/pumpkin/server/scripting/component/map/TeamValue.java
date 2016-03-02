package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.server.map.MapTeam;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;

public class TeamValue extends SimpleValue<Team> {

    public TeamValue(Team value) {
        super(value);
    }

    @Callback
    public Object[] setSubtitle(Context ctx, Arguments args){
        ((MapTeam) getValue()).setSubtitle(MapComponent.convertText(args, 0));
        return new Object[0];
    }

    @Callback
    public Object[] clearSubtitle(Context ctx, Arguments args){
        ((MapTeam) getValue()).clearSubtitle();
        return new Object[0];
    }

    @Callback
    public Object[] setSpawn(Context ctx, Arguments args){
        double x = args.checkDouble(0);
        double y = args.checkDouble(1);
        double z = args.checkDouble(2);
        double pitch = args.optDouble(3, 0);
        double yaw = args.optDouble(4, 0);
        getValue().setSpawn(x, y, z, (float) pitch, (float) yaw);
        return new Object[0];
    }
}
