package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

public class WorldValue extends SimpleValue<MapWorld> {

    public WorldValue(MapWorld value) {
        super(value);
    }

    @Callback
    public Object[] setTime(Context ctx, Arguments args){
        int time = args.checkInteger(0);
        if(time < 0 || time >= 24000){
            throw new IllegalArgumentException();
        }
        this.getValue().getWorld().getProperties().setWorldTime(time);
        return new Object[0];
    }

    @Callback
    public Object[] setDifficulty(Context ctx, Arguments args){
        String d = args.checkString(0);
        Difficulty difficulty;
        if(d.equals("peaceful")){
            difficulty = Difficulties.PEACEFUL;
        }else if(d.equals("easy")){
            difficulty = Difficulties.EASY;
        }else if(d.equals("normal")){
            difficulty = Difficulties.NORMAL;
        }else if(d.equals("hard")){
            difficulty = Difficulties.HARD;
        }else{
            throw new IllegalArgumentException();
        }
        getValue().getWorld().getProperties().setDifficulty(difficulty);
        return new Object[0];
    }
}
