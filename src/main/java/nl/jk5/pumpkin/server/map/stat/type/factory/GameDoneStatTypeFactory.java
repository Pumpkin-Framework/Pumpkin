package nl.jk5.pumpkin.server.map.stat.type.factory;

import com.google.gson.JsonObject;
import nl.jk5.pumpkin.api.mappack.game.stat.StatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.type.GameDoneStatType;

public class GameDoneStatTypeFactory implements StatTypeFactory<GameDoneStatType> {

    @Override
    public GameDoneStatType create(String name, JsonObject args) {
        return new GameDoneStatType(name);
    }
}
