package nl.jk5.pumpkin.server.map.stat.type;

import nl.jk5.pumpkin.api.mappack.game.stat.StatListener;

public class GameDoneStatType extends AbstractStatType {

    public GameDoneStatType(String name) {
        super(name);
    }

    public void setDone() {
        this.getListeners().forEach(StatListener::onTrigger);
    }

    public void reset() {
        this.getListeners().forEach(StatListener::onDisable);
    }
}
