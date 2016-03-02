package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.api.mappack.game.Winnable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;

public interface Team extends Winnable {

    String getName();

    TextColor getColor();

    Collection<Player> getMembers();

    boolean isFriendlyFireEnabled();

    @Override
    default Collection<Player> getWinners() {
        return this.getMembers();
    }

    @Override
    default Text getWinnableDescription() {
        return Text.of("Team ", this.getColor(), this.getName());
    }
}
