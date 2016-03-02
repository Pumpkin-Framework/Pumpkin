package nl.jk5.pumpkin.server.map;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.api.mappack.MappackTeam;
import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.api.utils.PlayerLocation;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapTeam implements Team {

    private final MappackTeam config;
    private final Map map;

    @Nullable
    private PlayerLocation spawnPoint;

    @Nullable
    private Text subtitle;

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

    @Override
    public void setSpawn(double x, double y, double z, float pitch, float yaw) {
        this.spawnPoint = new PlayerLocation(x, y, z, yaw, pitch);
    }

    @Override
    public Optional<PlayerLocation> getSpawnPoint() {
        return Optional.ofNullable(this.spawnPoint);
    }

    private void updateSubtitle(){
        if(this.subtitle == null) return;
        this.getMembers().forEach(p -> p.sendMessage(ChatTypes.ACTION_BAR, this.subtitle));
    }

    public void setSubtitle(Text subtitle) {
        this.subtitle = subtitle;
        this.updateSubtitle();
    }

    public void clearSubtitle() {
        this.subtitle = null;
    }

    public void onSubtitleTick() {
        this.updateSubtitle();
    }
}
