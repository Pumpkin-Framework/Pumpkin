package nl.jk5.pumpkin.sponge.commands;

import nl.jk5.pumpkin.sponge.commands.executors.FeedExecutor;
import nl.jk5.pumpkin.sponge.commands.executors.GameModeExecutor;
import nl.jk5.pumpkin.sponge.commands.executors.HealExecutor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.blockray.BlockRay;

@Plugin(id = "Pumpkin-Commands", name = "Pumpkin Commands", version = "1.0.0")
public class PumpkinCommandsPlugin {

    @Listener
    public void onInit(GameInitializationEvent event){
        event.getGame().getCommandDispatcher().register(this, GameModeExecutor.createRegistration(event.getGame()), "gamemode", "gm");
        event.getGame().getCommandDispatcher().register(this, HealExecutor.createRegistration(event.getGame()), "heal");
        event.getGame().getCommandDispatcher().register(this, FeedExecutor.createRegistration(event.getGame()), "feed");
    }
}
