package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import nl.jk5.pumpkin.server.utils.RegistryUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

import java.util.Optional;

public class PlayerValue extends SimpleValue<Player> {

    public PlayerValue(Player value) {
        super(value);
    }

    @Callback
    public Object[] setGamemode(Context ctx, Arguments args){
        Optional<GameMode> gameMode = RegistryUtils.gameModeByName(args.checkString(0));
        if(!gameMode.isPresent()){
            throw new IllegalArgumentException();
        }
        getValue().offer(Keys.GAME_MODE, gameMode.get());
        return new Object[0];
    }

    @Callback
    public Object[] clearInventory(Context ctx, Arguments args){
        getValue().getInventory().clear();
        return new Object[0];
    }

    @Callback
    public Object[] heal(Context ctx, Arguments args){
        getValue().offer(Keys.HEALTH, getValue().maxHealth().get());
        return new Object[0];
    }
}
