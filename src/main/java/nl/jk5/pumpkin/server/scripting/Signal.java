package nl.jk5.pumpkin.server.scripting;

/**
 * A single signal that was queued on a machine.
 * <p/>
 * This interface is not intended to be implemented, it only serves as a return
 * type for {@link Machine#popSignal()}.
 */
public interface Signal {

    /**
     * The name of the signal.
     */
    String getName();

    /**
     * The list of arguments for the signal.
     */
    Object[] getArgs();
}
