package nl.jk5.pumpkin.server.map.stat.type.factory;

import com.google.gson.JsonObject;
import nl.jk5.pumpkin.api.mappack.game.stat.StatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.type.GameRunningStatType;

public class GameRunningStatTypeFactory implements StatTypeFactory<GameRunningStatType> {

    @Override
    public GameRunningStatType create(String name, JsonObject args) {
        return new GameRunningStatType(name);
    }
}
