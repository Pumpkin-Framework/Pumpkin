package nl.jk5.pumpkin.api.mappack;

import nl.jk5.pumpkin.server.scripting.filesystem.IMount;

import java.util.Collection;
import java.util.Optional;

public interface Mappack {

    int getId();

    String getName();

    @SuppressWarnings("unchecked")
    abstract Collection<MappackAuthor> getAuthors();

    @SuppressWarnings("unchecked")
    Collection<MappackWorld> getWorlds();

    @SuppressWarnings("unchecked")
    Collection<MappackTeam> getTeams();

    Optional<IMount> createMount();
}
