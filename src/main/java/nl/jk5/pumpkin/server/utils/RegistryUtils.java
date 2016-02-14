package nl.jk5.pumpkin.server.utils;

import nl.jk5.pumpkin.server.world.gen.empty.DummyVoidGenerator;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;

import java.util.Optional;

public final class RegistryUtils {

    public static Optional<GameMode> gameModeByName(String name){
        if(name.equals("survival")){
            return Optional.of(GameModes.SURVIVAL);
        }else if(name.equals("creative")){
            return Optional.of(GameModes.CREATIVE);
        }else if(name.equals("adventure")){
            return Optional.of(GameModes.ADVENTURE);
        }else if(name.equals("spectator")){
            return Optional.of(GameModes.SPECTATOR);
        }
        return Optional.empty();
    }

    public static Optional<DimensionType> dimensionTypeByName(String name){
        if(name.equals("overworld")){
            return Optional.of(DimensionTypes.OVERWORLD);
        }else if(name.equals("nether")){
            return Optional.of(DimensionTypes.NETHER);
        }else if(name.equals("the-end")){
            return Optional.of(DimensionTypes.THE_END);
        }
        return Optional.empty();
    }

    public static Optional<GeneratorType> generatorTypeByName(String name){
        if(name.equals("default")){
            return Optional.of(GeneratorTypes.DEFAULT);
        }else if(name.equals("amplified")){
            return Optional.of(GeneratorTypes.AMPLIFIED);
        }else if(name.equals("debug")){
            return Optional.of(GeneratorTypes.DEBUG);
        }else if(name.equals("flat")){
            return Optional.of(GeneratorTypes.FLAT);
        }else if(name.equals("large-biomes")){
            return Optional.of(GeneratorTypes.LARGE_BIOMES);
        }else if(name.equals("nether")){
            return Optional.of(GeneratorTypes.NETHER);
        }else if(name.equals("overworld")){
            return Optional.of(GeneratorTypes.OVERWORLD);
        }else if(name.equals("the-end")){
            return Optional.of(GeneratorTypes.THE_END);
        }else if(name.equals("void")){
            //GeneratorType ret = GeneratorTypes.FLAT;
            //ret.getGeneratorSettings().set(DataQuery.of("customSettings"), "3;minecraft:air;127;decoration");
            //return Optional.of(ret);

            return Optional.of(new DummyVoidGenerator());
        }
        return Optional.empty();
    }
}
