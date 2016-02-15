package nl.jk5.pumpkin.server.player;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Charsets;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.sql.obj.DatabasePlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerRegistry {

    private final Pumpkin pumpkin;

    public PlayerRegistry(Pumpkin pumpkin) {
        this.pumpkin = pumpkin;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Login event) throws SQLException {
        List<DatabasePlayer> players = this.pumpkin.getTableManager().playerDao.queryForEq("uuid", event.getProfile().getUniqueId());
        DatabasePlayer player;
        boolean tpToLobby = true;
        MapWorld lobbyWorld = this.pumpkin.getMapRegistry().getLobby().getDefaultWorld();
        if(players.size() == 0){
            UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + event.getProfile().getName()).getBytes(Charsets.UTF_8));
            boolean offline = offlineId.equals(event.getProfile().getUniqueId());
            player = new DatabasePlayer();
            player.setOnlineMode(!offline);
            player.setUsername(event.getProfile().getName());
            player.setUuid(event.getProfile().getUniqueId());
            player.setServer(Pumpkin.getServerId());
            player.setWorldId(lobbyWorld.getConfig().getId());
            player.setX(lobbyWorld.getConfig().getSpawnpoint().getX());
            player.setY(lobbyWorld.getConfig().getSpawnpoint().getY());
            player.setZ(lobbyWorld.getConfig().getSpawnpoint().getZ());
            player.setPitch(lobbyWorld.getConfig().getSpawnpoint().getPitch());
            player.setYaw(lobbyWorld.getConfig().getSpawnpoint().getYaw());
            this.pumpkin.getTableManager().playerDao.create(player);
            tpToLobby = true;
        }else{
            player = players.get(0);
            if(player.getWorldId() == lobbyWorld.getConfig().getId()){
                tpToLobby = false;
                if(!player.getServer().equals(Pumpkin.getServerId())){
                    event.setToTransform(event.getToTransform()
                            .setLocation(new Location<>(lobbyWorld.getWorld(), player.getX(), player.getY(), player.getZ()))
                            .setRotation(new Vector3d(player.getPitch(), player.getYaw(), 0))
                    );
                }
            }
            if(player.getServer().equals(Pumpkin.getServerId())){
                tpToLobby = false;
            }
            player.setServer(Pumpkin.getServerId());
            this.pumpkin.getTableManager().playerDao.update(player);
        }

        if(tpToLobby){
            event.setToTransform(event.getToTransform()
                    .setLocation(lobbyWorld.getConfig().getSpawnpoint().toLocation(lobbyWorld.getWorld()))
                    .setRotation(lobbyWorld.getConfig().getSpawnpoint().getRotation())
            );
        }
    }

    @Listener
    public void onDisconect(ClientConnectionEvent.Disconnect event) throws SQLException {
        List<DatabasePlayer> players = this.pumpkin.getTableManager().playerDao.queryForEq("uuid", event.getTargetEntity().getUniqueId());
        if(players.isEmpty()){
            return;
        }

        DatabasePlayer player = players.get(0);
        Optional<MapWorld> world = this.pumpkin.getMapRegistry().getMapWorld(event.getTargetEntity().getWorld());
        if(!world.isPresent()){
            player.setWorldId(-1);
        }else{
            player.setWorldId(world.get().getConfig().getId());
        }
        player.setX(event.getTargetEntity().getLocation().getX());
        player.setY(event.getTargetEntity().getLocation().getY());
        player.setZ(event.getTargetEntity().getLocation().getZ());
        player.setPitch((float) event.getTargetEntity().getRotation().getX());
        player.setYaw((float) event.getTargetEntity().getRotation().getY());
        this.pumpkin.getTableManager().playerDao.update(player);
    }
}