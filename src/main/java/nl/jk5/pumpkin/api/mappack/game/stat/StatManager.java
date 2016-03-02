package nl.jk5.pumpkin.api.mappack.game.stat;

import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.server.map.stat.StatEmitter;

import java.util.Collection;
import java.util.function.Consumer;

public interface StatManager {

    Collection<StatEmitter> getStatEmitters(MapWorld world);

    void addStatEmitter(StatEmitter emitter);

    <T extends StatType> Collection<T> getAllOf(Class<T> type);

    <T extends StatType> void with(Class<T> type, Consumer<T> method);

    StatType byName(String name);
}
