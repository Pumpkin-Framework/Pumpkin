package nl.jk5.pumpkin.server.scripting.builder;

import nl.jk5.pumpkin.server.scripting.*;
import nl.jk5.pumpkin.server.scripting.component.Component;
import nl.jk5.pumpkin.server.scripting.component.Environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleComponent extends MutableNode implements Component {

    private final Environment host;
    private final String name;
    private final Map<String, Callbacks.Callback> callbacks;

    private final Map<String, Environment> hosts = new HashMap<>();

    public SimpleComponent(Environment host, nl.jk5.pumpkin.api.mappack.Map map, String name) {
        super(host, map);
        this.host = host;
        this.name = name;

        this.callbacks = Callbacks.search(this.host);

        this.callbacks.forEach((method, callback) -> this.hosts.put(method, this.host));
    }

    @Override
    public Collection<String> methods() {
        return this.callbacks.keySet();
    }

    @Override
    public Callback annotation(String method) throws NoSuchMethodException {
        if(!this.callbacks.containsKey(method)){
            throw new NoSuchMethodException();
        }
        return this.callbacks.get(method).getAnnotation();
    }

    @Override
    public Object[] invoke(String method, Context context, Object... arguments) throws Exception {
        if(!this.callbacks.containsKey(method)){
            throw new NoSuchMethodException();
        }
        if(!this.hosts.containsKey(method)){
            throw new NoSuchMethodException();
        }
        return Registry.convert(context, this.callbacks.get(method).apply(this.hosts.get(method), context, new ArgumentsImpl(arguments)));
    }

    @Override
    public String getName() {
        return name;
    }
}
