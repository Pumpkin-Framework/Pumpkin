package nl.jk5.pumpkin.api.mappack.game;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Collections;

public class WinnablePlayerWrapper implements Winnable {

    private final Player player;

    public WinnablePlayerWrapper(Player player) {
        this.player = player;
    }

    @Override
    public Collection<Player> getWinners() {
        return Collections.singletonList(this.player);
    }

    @Override
    public Text getWinnableDescription() {
        return Text.of(TextColors.YELLOW, this.player.getName());
    }
}
