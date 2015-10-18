package nl.jk5.pumpkin.sponge.commands;

import nl.jk5.pumpkin.sponge.commands.executors.FeedExecutor;
import nl.jk5.pumpkin.sponge.commands.executors.GameModeExecutor;
import nl.jk5.pumpkin.sponge.commands.executors.HealExecutor;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.text.selector.Selectors;

import javax.inject.Inject;

@Plugin(id = "Pumpkin-Commands", name = "Pumpkin Commands", version = "1.0.0")
public class PumpkinCommandsPlugin {

    @Inject
    private Logger logger;

    @Listener
    public void onInit(GameInitializationEvent event){
        event.getGame().getCommandDispatcher().register(this, GameModeExecutor.createRegistration(event.getGame()), "gamemode", "gm");
        event.getGame().getCommandDispatcher().register(this, HealExecutor.createRegistration(event.getGame()), "heal");
        event.getGame().getCommandDispatcher().register(this, FeedExecutor.createRegistration(event.getGame()), "feed");

        logger.info(Selectors.builder(SelectorTypes.ALL_PLAYERS).add(ArgumentTypes.LEVEL.minimum(), 5).add(ArgumentTypes.LEVEL.maximum(), 5).add(ArgumentTypes.NAME, "WUT").build().toPlain());


    }
}
