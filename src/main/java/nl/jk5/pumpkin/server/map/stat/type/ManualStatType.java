package nl.jk5.pumpkin.server.map.stat.type;

import nl.jk5.pumpkin.api.mappack.game.stat.StatListener;

public class ManualStatType extends AbstractStatType {

    public ManualStatType(String name) {
        super(name);
    }

    public void enable(){
        this.getListeners().forEach(StatListener::onEnable);
    }

    public void disable(){
        this.getListeners().forEach(StatListener::onDisable);
    }

    public void trigger(){
        this.getListeners().forEach(StatListener::onTrigger);
    }
}
