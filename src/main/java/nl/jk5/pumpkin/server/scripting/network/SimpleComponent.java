package nl.jk5.pumpkin.server.scripting.network;

import nl.jk5.pumpkin.server.scripting.*;
import nl.jk5.pumpkin.server.scripting.component.Component;

import java.util.Collection;
import java.util.Map;

public class SimpleComponent extends MutableNode implements Component {

    private final String name;
    private final Map<String, Callbacks.Callback> callbacks;

    public SimpleComponent(Environment host, String name) {
        super(host);
        this.name = name;
        this.callbacks = Callbacks.search(host);
    }

    @Override
    public String address() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public Collection<String> methods() {
        return callbacks.keySet();
    }

    @Override
    public Callback annotation(String method) throws NoSuchMethodException {
        Callbacks.Callback callback = this.callbacks.get(method);
        if(callback == null){
            throw new NoSuchMethodException();
        }
        return callback.getAnnotation();
    }

    @Override
    public Object[] invoke(String method, Context context, Object... arguments) throws Exception {
        Callbacks.Callback callback = this.callbacks.get(method);
        if(callback == null){
            throw new NoSuchMethodException();
        }
        return Registry.convert(callback.apply(host(), context, new ArgumentsImpl(arguments)));
    }

    @Override
    public String toString() {
        return super.toString() + "@" + this.name;
    }
}
