package nl.jk5.pumpkin.api.mappack.game;

import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;

public interface Winnable {

    Collection<Player> getPlayers();
}
