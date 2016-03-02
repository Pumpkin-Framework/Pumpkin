package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.api.mappack.game.stat.StatConfig;

import java.util.Collection;

public interface Mappack {

    int getId();

    String getName();

    Collection<MappackAuthor> getAuthors();

    Collection<MappackWorld> getWorlds();

    Collection<MappackTeam> getTeams();

    Collection<StatConfig> getStats();

    //Optional<IMount> createMount();
}
