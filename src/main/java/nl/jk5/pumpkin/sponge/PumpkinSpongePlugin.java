package nl.jk5.pumpkin.sponge;

import nl.jk5.pumpkin.sponge.services.PumpkinServiceManger;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ConstructWorldEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

@Plugin(id = "Pumpkin", name = "Pumpkin", version = "1.0.0", dependencies = "required-after:PumpkinWorldHooks@[1.1.0,)")
public class PumpkinSpongePlugin {

    @Inject public Logger logger;
    @Inject public Game game;

    private PumpkinServiceManger serviceManager;

    private static World lobby;

    @Listener
    public void onPreInit(GamePreInitializationEvent event){
        this.serviceManager = new PumpkinServiceManger(game);
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event){
        this.serviceManager.getWorldHooksService().unregisterDimension(-1);
        this.serviceManager.getWorldHooksService().unregisterDimension(1);
        this.serviceManager.getWorldHooksService().releaseDimensionId(-1);
        this.serviceManager.getWorldHooksService().releaseDimensionId(1);

        //To change the dimension type of the lobby
        this.serviceManager.getWorldHooksService().unregisterDimension(0);
        this.serviceManager.getWorldHooksService().registerDimension(0, -1);
        //To change the generator type, see MixinMinecraftServer.loadAllWorlds
        //The worldInfo var is loaded from the level.dat file. To change it in the code we need to patch that loading code
        //Void generator setting: 3;minecraft:air;127;decoration
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        //lobby = game.getRegistry().createWorldBuilder().dimensionType(DimensionTypes.NETHER).generator(GeneratorTypes.FLAT).name("lobby").enabled(true).loadsOnStartup(true).build().orElse(null);
        //Log.info("Lobby effictive dimension type: {}", lobby.getDimension().getName());
        //Log.info("Lobby WorldProvider: {}", lobby.getDimension().getClass().getName());
        //Log.info(lobby.toString());

        this.serviceManager.getWorldHooksService().setDimensionType(game.getServer().getDefaultWorld().get(), DimensionTypes.NETHER);
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event){
        this.serviceManager.getWorldHooksService().setDimensionType(game.getServer().getDefaultWorld().get(), DimensionTypes.NETHER);
    }

    @Listener
    public void playerLoggedIn(ClientConnectionEvent.Login event){
        //event.setToTransform(new Transform<>(lobby));
        logger.info("Effective dimension type: {}", event.getFromTransform().getExtent().getDimension().getType());
        logger.info("WorldProvider: {}", event.getFromTransform().getExtent().getDimension().getClass().getName());
    }

    @Listener
    public void onWorldCreate(ConstructWorldEvent event){
        logger.info("World construct {}", event.getWorldProperties().getWorldName());
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event){
        logger.info("World load {}", event.getTargetWorld().getProperties().getWorldName());
        if(this.serviceManager.getWorldHooksService().getDimensionId(event.getTargetWorld()) == 0){
            this.serviceManager.getWorldHooksService().setDimensionType(event.getTargetWorld().getProperties(), DimensionTypes.NETHER);
        }
    }
}
