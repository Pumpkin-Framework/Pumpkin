package nl.jk_5.pumpkin;

import static org.spongepowered.api.service.permission.SubjectData.GLOBAL_CONTEXT;
import static org.spongepowered.api.util.Tristate.FALSE;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;

import nl.jk_5.pumpkin.command.PumpkinCommands;
import nl.jk_5.pumpkin.hooks.PumpkinHooksService;

@Plugin(id = "pumpkin", name = "Pumpkin", version = "1.0.0-SNAPSHOT", dependencies = "required-after:pumpkin-hooks")
public class PumpkinPlugin {

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Subscribe
    public void onServerPreStart(ServerAboutToStartEvent event){
        logger.info("Pumpkin initializing...");

        PumpkinCommands.init(this, game.getCommandDispatcher());

        PermissionService service = event.getGame().getServiceManager().provide(PermissionService.class).get();
        service.getDefaultData().setPermission(GLOBAL_CONTEXT, "pumpkin.command.test", FALSE);

        PumpkinHooksService hooks = game.getServiceManager().provide(PumpkinHooksService.class).orNull();
        if(hooks == null){
            logger.info("Pumpkin Hooks service was not found");
            throw new RuntimeException("Pumpkin Hooks service missing");
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent event){

    }
}
