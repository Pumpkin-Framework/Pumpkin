package nl.jk5.pumpkin.api.mappack;

import org.spongepowered.api.text.format.TextColor;

public interface MappackTeam {

    int getId();

    String getName();

    TextColor getColor();

    Mappack getMappack();
}
