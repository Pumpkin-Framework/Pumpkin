package nl.jk5.pumpkin.server.scripting.builder;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.server.scripting.component.Environment;
import nl.jk5.pumpkin.server.scripting.component.Node;

public final class NodeBuilder {

    private final Environment host;
    private final Map map;

    public NodeBuilder(Environment host, Map map){
        this.host = host;
        this.map = map;
    }

    public ComponentBuilder withComponent(String name){
        return new ComponentBuilder(this.host, this.map, name);
    }

    public Node create(){
        return new MutableNode(this.host, this.map);
    }
}
