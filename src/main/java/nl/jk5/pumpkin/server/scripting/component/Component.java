package nl.jk5.pumpkin.server.scripting.component;

import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;

import java.util.Collection;

public interface Component {

    String address();

    String type();

    Collection<String> methods();

    Callback annotation(String method);

    Object[] invoke(String method, Context context, Object... arguments) throws Exception;
}
