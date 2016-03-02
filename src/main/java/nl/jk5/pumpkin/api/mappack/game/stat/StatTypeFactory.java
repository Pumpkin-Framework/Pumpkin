package nl.jk5.pumpkin.api.mappack.game.stat;

import com.google.gson.JsonObject;

public interface StatTypeFactory<T extends StatType> {

    T create(String name, JsonObject args);
}
