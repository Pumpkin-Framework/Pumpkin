package nl.jk5.pumpkin.server.map.stat.type;

import nl.jk5.pumpkin.api.mappack.game.stat.StatListener;
import nl.jk5.pumpkin.api.mappack.game.stat.StatType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class AbstractStatType implements StatType {

    private final String name;

    private List<StatListener> listeners = new ArrayList<>();

    public AbstractStatType(String name) {
        this.name = name;
    }

    @Override
    public void registerListener(StatListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregisterListener(StatListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Collection<StatListener> getListeners() {
        return this.listeners;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

