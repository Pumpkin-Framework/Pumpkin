package nl.jk5.pumpkin.server.map.stat.type.factory;

import com.google.gson.JsonObject;
import nl.jk5.pumpkin.api.mappack.game.stat.StatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.type.ManualStatType;

public class ManualStatTypeFactory implements StatTypeFactory<ManualStatType> {

    @Override
    public ManualStatType create(String name, JsonObject args) {
        return new ManualStatType(name);
    }
}
