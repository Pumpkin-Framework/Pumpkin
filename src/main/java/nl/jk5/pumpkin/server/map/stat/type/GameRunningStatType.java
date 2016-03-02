package nl.jk5.pumpkin.server.map.stat.type;

import nl.jk5.pumpkin.api.mappack.game.stat.StatListener;

public class GameRunningStatType extends AbstractStatType {

    public GameRunningStatType(String name) {
        super(name);
    }

    public void setRunning() {
        this.getListeners().forEach(StatListener::onEnable);
    }

    public void setNotRunning() {
        this.getListeners().forEach(StatListener::onDisable);
    }
}
