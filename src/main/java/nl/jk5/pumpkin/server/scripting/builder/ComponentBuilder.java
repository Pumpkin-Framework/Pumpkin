package nl.jk5.pumpkin.server.scripting.builder;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.server.scripting.component.Component;
import nl.jk5.pumpkin.server.scripting.component.Environment;

public final class ComponentBuilder {

    private final Environment host;
    private final Map map;
    private final String name;

    public ComponentBuilder(Environment host, Map map, String name) {
        this.host = host;
        this.map = map;
        this.name = name;
    }

    public Component create(){
        return new SimpleComponent(this.host, this.map, this.name);
    }
}
