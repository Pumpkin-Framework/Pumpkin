package nl.jk5.pumpkin.api.mappack;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;

public interface Team {

    String getName();

    TextColor getColor();

    Collection<Player> getMembers();
}
