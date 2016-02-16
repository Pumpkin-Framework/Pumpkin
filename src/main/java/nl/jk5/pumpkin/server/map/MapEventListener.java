package nl.jk5.pumpkin.server.map;

import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;

public final class MapEventListener {

    private final Pumpkin pumpkin;

    public MapEventListener(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event){
        //if(!event.isBedSpawn()) {
        //    Location<World> location = setSpawn(event.getToTransform(), event.getTargetEntity());
        //    event.setToTransform(event.getToTransform().setLocation(location));
        //}
    }
}
