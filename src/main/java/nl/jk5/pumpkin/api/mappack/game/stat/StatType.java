package nl.jk5.pumpkin.api.mappack.game.stat;

import java.util.Collection;

public interface StatType {

    String getName();

    void registerListener(StatListener listener);

    void unregisterListener(StatListener listener);

    Collection<StatListener> getListeners();
}
