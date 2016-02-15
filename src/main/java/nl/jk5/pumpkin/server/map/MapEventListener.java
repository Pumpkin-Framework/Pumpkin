package nl.jk5.pumpkin.server.map;

import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.Pumpkin;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;

public final class MapEventListener {

    private final Pumpkin pumpkin;

    public MapEventListener(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    //@Listener
    public void onJoin(ClientConnectionEvent.Login event){
        Optional<JoinData> joinData = event.getTargetUser().get(JoinData.class);
        if(joinData.isPresent()){
            if(joinData.get().firstPlayed().equals(joinData.get().lastPlayed())){ // Player is online for the first time
                MapWorld lobbyWorld = this.pumpkin.getMapRegistry().getLobby().getDefaultWorld();
                event.setToTransform(event.getToTransform()
                        .setLocation(lobbyWorld.getConfig().getSpawnpoint().toLocation(lobbyWorld.getWorld()))
                        .setRotation(lobbyWorld.getConfig().getSpawnpoint().getRotation())
                );
            }else{ // Player has been online before

            }
        }
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event){
        //if(!event.isBedSpawn()) {
        //    Location<World> location = setSpawn(event.getToTransform(), event.getTargetEntity());
        //    event.setToTransform(event.getToTransform().setLocation(location));
        //}
    }
}
