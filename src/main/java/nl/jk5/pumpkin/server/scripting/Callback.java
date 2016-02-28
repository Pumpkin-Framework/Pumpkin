package nl.jk5.pumpkin.server.scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used for methods in an {@link nl.jk_5.pumpkin.server.lua.network.Environment} to mark
 * them for exposure to computers.
 * <p/>
 * Any method exposed like this can be enumerated and called from a computer
 * that can see the node of the environment.
 * <p/>
 * Note that methods annotated with this interface must have the following
 * signature:
 * <pre>
 *     Object[] f(Context context, Arguments arguments) throws Exception;
 * </pre>
 * <p/>
 * The method may return <tt>null</tt> in case it doesn't wish return anything,
 * which is functionally equivalent to returning an empty array.
 * <p/>
 * To raise an error from your callback, simply throw an exception. The
 * convention for Lua is to return (null, "reason") for 'soft' errors, i.e.
 * errors that are no fault of the caller. For example, passing invalid
 * arguments will generate an exception, requesting information and the lookup
 * of said information failing should not.
 *
 * @see Context
 * @see nl.jk_5.pumpkin.server.lua.Arguments
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Callback {

    /**
     * The name under which to make the callback available.
     * <p/>
     * This defaults to the name of the annotated method if left empty.
     */
    String value() default "";

    /**
     * Whether this function may be called directly from the computer's executor
     * thread instead of from the server thread.
     * <p/>
     * You will have to ensure anything your callback does is thread safe when
     * setting this to <tt>true</tt>. Use this for minor lookups, for example.
     * This is mainly intended to allow functions to perform faster than when
     * called 'synchronously' (where the call takes at least one server tick).
     */
    boolean direct() default false;

    /**
     * The maximum number of direct calls that may be performed on this
     * component in a single <em>tick</em> on a tier two computer.
     * This can be used to throttle call speed.
     * <p/>
     * You should generally apply a limit if the callback allocates persisting
     * memory (i.e. memory that isn't freed once the call returns), sends
     * network messages, or uses any other kind of resource for which it'd be
     * bad if it were to be used from user programs in an unchecked, unregulated
     * manner.
     * <p/>
     * Note that the limit does <em>not</em> apply when the method is invoked
     * via a direct call to {@link nl.jk_5.pumpkin.server.lua.network.Component#invoke(String, Context, Object...)}
     * from the host side. Also, this limit is per-machine, so the method may
     * be invoked more often than this per tick, if different machines call it.
     * <p/>
     * This value is a general cost value: for each direct call a computer makes,
     * <tt>1/limit</tt> is subtracted from the computer's call budget.
     */
    int limit() default Integer.MAX_VALUE;

    /**
     * A documentation string that is made available to the computers the
     * component this callback belongs to is connected to. This allows for
     * ingame documentation of callbacks.
     * <p/>
     * You may want to give a short description of what a method does here, but
     * more importantly you should document the expected parameters and return
     * type here.
     * <p/>
     * <em>Important</em>: the recommended format is either<br/>
     * <tt>function(arg:type[, optionArg:type]):resultType -- Description.</tt><br/>
     * or<br/>
     * <tt>function(arg:type[, optionArg:type]):resultType; Description.</tt><br/>
     * where the argument list can be of any format (as long as it doesn't contain
     * further braces), and the return type is optional.
     */
    String doc() default "";

    /**
     * Whether this callback should work like a getter.
     * <p/>
     * Callbacks that are getters do not appear as methods on a component's
     * proxy. Instead they are accessed as fields, for example in Lua via the
     * proxy's <tt>__index</tt> metamethod, with its only parameter being the
     * accessed key.
     * <p/>
     * Note: if you wish to have a field that is read/write, that is you need
     * both a getter and a setter, you have to implement them in the same
     * method. This a limitation due to callback names being unique. You can
     * differentiate between contexts by checking the number of arguments.
     * <p/>
     * <em>Important</em>: this only works in environments (for components),
     * it does <em>not</em> work for userdata (<tt>Value</tt> objects). For
     * userdata, use the <tt>apply</tt> method instead.
     */
    boolean getter() default false;

    /**
     * Whether this callback should work like a setter.
     * <p/>
     * Callbacks that are setters do not appear as methods on a component's
     * proxy. Instead they are accessed as fields, for example in Lua via the
     * proxy's <tt>__newindex</tt> metamethod, with its only two parameters
     * being the accessed key and the new value.
     * <p/>
     * Note: if you wish to have a field that is read/write, that is you need
     * both a getter and a setter, you have to implement them in the same
     * method. This a limitation due to callback names being unique. You can
     * differentiate between contexts by checking the number of arguments.
     * <p/>
     * <em>Important</em>: this only works in environments (for components),
     * it does <em>not</em> work for userdata (<tt>Value</tt> objects). For
     * userdata, use the <tt>unapply</tt> method instead.
     */
    boolean setter() default false;
}