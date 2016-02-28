package nl.jk5.pumpkin.server.scripting.component;

import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;

import java.util.Collection;

public interface Component extends Node {

    /**
     * The list of names of methods exposed by this component.
     * <p/>
     * This does not return the callback annotations directly, because those
     * may not contain the method's name (as it defaults to the name of the
     * annotated method).
     * <p/>
     * The returned collection is read-only.
     */
    Collection<String> methods();

    /**
     * Get the annotation information of a method.
     * <p/>
     * This is needed for custom architecture implementations that need to know
     * if a callback is direct or not, for example.
     *
     * @param method the method to the the info for.
     * @return the annotation of the specified method or <tt>null</tt>.
     */
    Callback annotation(String method) throws NoSuchMethodException;

    /**
     * Tries to call a function with the specified name on this component.
     * <p/>
     * The name of the method must be one of the names in {@link #methods()}.
     * The returned array may be <tt>null</tt> if there is no return value.
     *
     * @param method    the name of the method to call.
     * @param context   the context from which the method is called, usually the
     *                  instance of the computer running the script that made
     *                  the call.
     * @param arguments the arguments passed to the method.
     * @return the list of results, or <tt>null</tt> if there is no result.
     * @throws NoSuchMethodException if there is no method with that name.
     */
    Object[] invoke(String method, Context context, Object... arguments) throws Exception;

    String getAddress();

    String getName();
}
