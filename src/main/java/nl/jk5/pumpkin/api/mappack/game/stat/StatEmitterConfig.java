package nl.jk5.pumpkin.api.mappack.game.stat;

import nl.jk5.pumpkin.api.mappack.MappackWorld;

public interface StatEmitterConfig {

    int getId();

    MappackWorld getWorld();

    StatConfig getStat();

    int getX();

    int getY();

    int getZ();
}
