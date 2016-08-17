package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.map.MapEventListener;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

import java.util.Optional;

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

    @Callback
    public Object[] watchBlockDestroy(Context ctx, Arguments args){
        int x = args.checkInteger(0);
        int y = args.checkInteger(1);
        int z = args.checkInteger(2);
        MapEventListener.registerBlockDestroyWatcher(this.getValue(), x, y, z);
        return new Object[]{"block_break_" + getValue().getConfig().getName() + "_" + x + "," + y + "," + z};
    }

    @Callback(direct = true)
    public Object[] setBlock(Context ctx, Arguments args){
        int x = args.checkInteger(0);
        int y = args.checkInteger(1);
        int z = args.checkInteger(2);
        String blockName = args.checkString(3);
        Optional<BlockType> type = Pumpkin.instance().getGame().getRegistry().getType(BlockType.class, blockName);
        if(!type.isPresent()){
            throw new IllegalArgumentException("Block type " + blockName + " does not exist");
        }
        this.getValue().getWorld().setBlock(x, y, z, BlockState.builder().blockType(type.get()).build(), Cause.of(NamedCause.of("plugin", Pumpkin.instance().getPluginContainer())));
        return new Object[0];
    }
}
