package nl.jk_5.pumpkin;

import static org.spongepowered.api.service.permission.SubjectData.GLOBAL_CONTEXT;
import static org.spongepowered.api.util.Tristate.FALSE;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerRespawnEvent;
import org.spongepowered.api.event.server.StatusPingEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.event.world.WorldCreateEvent;
import org.spongepowered.api.event.world.WorldLoadEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulties;

import nl.jk_5.pumpkin.command.PumpkinCommands;

@Plugin(id = "pumpkin", name = "Pumpkin", version = "1.0.0-SNAPSHOT")
public class PumpkinPlugin {

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    private World world2;

    @Subscribe
    public void onServerPreStart(ServerAboutToStartEvent event){
        logger.info("Pumpkin initializing...");

        PumpkinCommands.init(this, event.getGame().getCommandDispatcher());

        PermissionService service = event.getGame().getServiceManager().provide(PermissionService.class).get();
        service.getDefaultData().setPermission(GLOBAL_CONTEXT, "pumpkin.command.test", FALSE);

        world2 = game.getRegistry().getWorldBuilder().dimensionType(DimensionTypes.OVERWORLD).enabled(true).gameMode(GameModes.ADVENTURE).generator(GeneratorTypes.FLAT).keepsSpawnLoaded(false).loadsOnStartup(false).name("map_1_1").build().get();
    }

    @Subscribe
    public void onWorldCreate(WorldCreateEvent event){
        logger.info("World " + event.getWorldCreationSettings().getWorldName() + " is being created");
        event.getWorldProperties().setGeneratorType(GeneratorTypes.FLAT);
        event.getWorldProperties().setDifficulty(Difficulties.PEACEFUL);
        if(event.getWorldCreationSettings().getDimensionType() != DimensionTypes.OVERWORLD){
            event.getWorldProperties().setKeepSpawnLoaded(false);
            event.getWorldProperties().setLoadOnStartup(false);
        }
    }

    @Subscribe
    public void onWorldLoad(WorldLoadEvent event){

    }

    @Subscribe
    public void onPlayerJoin(PlayerJoinEvent event){
        //event.setLocation(world2.getSpawnLocation());
    }

    @Subscribe
    public void onRespawn(PlayerRespawnEvent event){
        event.setRespawnLocation(game.getServer().getWorld("world").get().getSpawnLocation());
    }

    @Subscribe
    public void onMultiplayerStatus(StatusPingEvent event){
        String input = event.getClient().getVirtualHost().get().getHostString();
        event.getResponse().setDescription(Texts.of(input));
        event.getResponse().getPlayers().get().setMax(9001);
    }

    @Subscribe
    public void onChat(PlayerChatEvent event){

    }
}
