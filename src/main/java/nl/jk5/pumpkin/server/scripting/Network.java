package nl.jk5.pumpkin.server.scripting;

import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.server.scripting.builder.NodeBuilder;
import nl.jk5.pumpkin.server.scripting.component.Environment;

public final class Network {

    public static NodeBuilder newNode(Environment environment, Map map){
        return new NodeBuilder(environment, map);
    }
}
