package nl.jk5.pumpkin.api.mappack.game;

import org.spongepowered.api.entity.living.player.Player;

public interface Game {

    GameStartResult start();

    boolean isRunning();

    boolean isInActiveGame(Player player);

    void onPlayerJoin(Player player);

    void onPlayerLeft(Player player);
}
