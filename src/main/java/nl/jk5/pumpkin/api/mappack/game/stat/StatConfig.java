package nl.jk5.pumpkin.api.mappack.game.stat;

import nl.jk5.pumpkin.api.mappack.Mappack;

public interface StatConfig {

    int getId();

    Mappack getMappack();

    String getName();

    String getType();

    String getArguments();
}
