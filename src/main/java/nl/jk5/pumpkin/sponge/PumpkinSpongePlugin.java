package nl.jk5.pumpkin.sponge;

import nl.jk5.pumpkin.sponge.services.PumpkinServiceManger;
import nl.jk5.pumpkin.sponge.utils.WorldUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = "Pumpkin", name = "Pumpkin", version = "1.0.0", dependencies = "required-after:PumpkinWorldHooks@[1.1.0,)")
public class PumpkinSpongePlugin {

    @Inject public Logger logger;
    @Inject public Game game;

    private PumpkinServiceManger serviceManager;

    @Listener
    public void onPreInit(GamePreInitializationEvent event){
        this.serviceManager = new PumpkinServiceManger(game);
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event){
        WorldUtils.unregisterDimension(-1);
        WorldUtils.unregisterDimension(1);
        WorldUtils.releaseDimensionId(-1);
        WorldUtils.releaseDimensionId(1);

        //To change the dimension type of the lobby
        WorldUtils.unregisterDimension(0);
        WorldUtils.registerDimension(0, -1);
        //To change the generator type, see MixinMinecraftServer.loadAllWorlds
        //The worldInfo var is loaded from the level.dat file. To change it in the code we need to patch that loading code
        //Void generator setting: 3;minecraft:air;127;decoration
        //Lava generator setting: 3;minecraft:bedrock,16*minecraft:lava;8;
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        //lobby = game.getRegistry().createWorldBuilder().dimensionType(DimensionTypes.NETHER).generator(GeneratorTypes.FLAT).name("lobby").enabled(true).loadsOnStartup(true).build().orElse(null);
    }
}
