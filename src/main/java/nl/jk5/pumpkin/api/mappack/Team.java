package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.api.mappack.game.Winnable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;

public interface Team extends Winnable {

    String getName();

    TextColor getColor();

    Collection<Player> getMembers();

    boolean isFriendlyFireEnabled();

    @Override
    default Collection<Player> getPlayers() {
        return this.getMembers();
    }
}
