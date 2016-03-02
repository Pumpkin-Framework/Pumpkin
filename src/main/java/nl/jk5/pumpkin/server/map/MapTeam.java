package nl.jk5.pumpkin.server.map;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MappackTeam;
import nl.jk5.pumpkin.api.mappack.Team;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;

import java.util.Collection;
import java.util.stream.Collectors;

public class MapTeam implements Team {

    private final MappackTeam config;
    private final Map map;

    public MapTeam(MappackTeam config, Map map){
        this.config = config;
        this.map = map;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public TextColor getColor() {
        return config.getColor();
    }

    @Override
    public Collection<Player> getMembers(){
        return this.map.getPlayerTeams().entrySet()
                .stream()
                .filter(e -> e.getValue() == this)
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFriendlyFireEnabled() {
        return this.config.isFriendlyFireEnabled();
    }
}
