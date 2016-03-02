package nl.jk5.pumpkin.api.mappack.game;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collection;

public interface Winnable {

    Collection<Player> getWinners();

    Text getWinnableDescription();
}
