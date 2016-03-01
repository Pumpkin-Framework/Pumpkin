package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.api.mappack.game.Game;
import nl.jk5.pumpkin.server.scripting.MachineHost;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Map extends MessageChannel, MachineHost {

    Collection<MapWorld> getWorlds();

    MapWorld getDefaultWorld();

    Collection<Team> getTeams();

    void addPlayerToTeam(Player player, Team team);

    void removePlayerFromTeam(Player player);

    Optional<Team> getPlayerTeam(Player player);

    Optional<Team> teamByName(String name);

    java.util.Map<UUID, Team> getUserTeams();

    java.util.Map<Player, Team> getPlayerTeams();

    void tick();

    File getSaveDirectory();

    Collection<Player> getPlayers();

    Optional<Game> getGame();

    boolean isInActiveGame(Player player);
}
