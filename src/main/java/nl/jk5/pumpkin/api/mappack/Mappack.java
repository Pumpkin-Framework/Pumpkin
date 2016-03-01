package nl.jk5.pumpkin.api.mappack;

import java.util.Collection;

public interface Mappack {

    int getId();

    String getName();

    @SuppressWarnings("unchecked")
    abstract Collection<MappackAuthor> getAuthors();

    @SuppressWarnings("unchecked")
    Collection<MappackWorld> getWorlds();

    @SuppressWarnings("unchecked")
    Collection<MappackTeam> getTeams();

    //Optional<IMount> createMount();
}
