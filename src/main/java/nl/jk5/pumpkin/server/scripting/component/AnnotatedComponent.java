package nl.jk5.pumpkin.server.scripting.component;

import com.google.common.collect.ImmutableMap;
import nl.jk5.pumpkin.server.scripting.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public abstract class AnnotatedComponent implements Component {

    private final Map<String, Callbacks.Callback> callbacks = ImmutableMap.copyOf(Callbacks.search(this));
    private final String type;

    public AnnotatedComponent() {
        Type type = this.getClass().getDeclaredAnnotation(Type.class);
        if(type == null){
            this.type = null;
            return;
        }
        this.type = type.value();
    }

    @Override
    public final String type() {
        return this.type;
    }

    @Override
    public final Collection<String> methods() {
        return this.callbacks.keySet();
    }

    @Override
    public final Callback annotation(String method) {
        if(!this.callbacks.containsKey(method)){
            throw new NoSuchElementException();
        }
        return this.callbacks.get(method).getAnnotation();
    }

    @Override
    public final Object[] invoke(String method, Context context, Object... arguments) throws Exception {
        if(!this.callbacks.containsKey(method)){
            throw new NoSuchElementException();
        }
        Callbacks.Callback callback = this.callbacks.get(method);
        return Registry.convert(callback.apply(this, context, new ArgumentsImpl(arguments)));
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface Type {
        String value();
    }
}
