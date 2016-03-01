package nl.jk5.pumpkin.server.scripting;

import com.google.common.collect.Multimap;
import nl.jk5.pumpkin.server.scripting.architecture.Architecture;
import nl.jk5.pumpkin.server.scripting.component.Component;
import nl.jk5.pumpkin.server.scripting.filesystem.FileSystem;
import nl.jk5.pumpkin.server.scripting.network.ManagedEnvironment;

import java.util.Map;
import java.util.Optional;

public interface Machine extends ManagedEnvironment, Context {

    /**
     * The owner of the machine, usually a mappack hosting the machine.
     *
     * @return the owner of the machine.
     */
    MachineHost host();

    /**
     * This must be called from the host when something relevant to the
     * machine setup changes, such as a change in the amount of available memory.
     */
    void onHostChanged();

    /**
     * The underlying architecture of the machine.
     * <p/>
     * This is what actually evaluates code running on the machine, where the
     * machine class itself serves as a scheduler.
     *
     * @return the architecture of this machine.
     */
    Architecture architecture();

    /**
     * The address of the file system that holds the machine's temporary files
     * (tmpfs). This may return <tt>null</tt> if either the creation of the file
     * system failed, or if the size of the tmpfs has been set to zero in the
     * config.
     * <p/>
     * Use this in a custom architecture to allow code do differentiate the
     * tmpfs from other file systems, for example.
     *
     * @return the address of the tmpfs component, or <tt>null</tt>.
     */
    String tmpAddress();

    /**
     * A string with the last error message.
     * <p/>
     * The error string is set either when the machine crashes (see the
     * {@link #crash(String)} method), or when it fails to start (which,
     * technically, is also a crash).
     * <p/>
     * When the machine started, this is reset to <tt>null</tt>.
     *
     * @return the last error message, or <tt>null</tt>.
     */
    String lastError();

    /**
     * The time that has passed since the machine was started, in seconds.
     * <p/>
     * Note that this is actually measured in world time, so the resolution is
     * pretty limited. This is done to avoid 'time skips' when leaving the game
     * and coming back later, resuming a persisted machine.
     */
    double upTime();

    /**
     * The time spent running the underlying architecture in execution threads,
     * i.e. the time spent in {@link Architecture#runThreaded(boolean)} since
     * the machine was last started, in seconds.
     */
    double cpuTime();

    /**
     * Crashes the computer.
     * <p/>
     * This is exactly the same as {@link Context#stop()}, except that it also
     * sets the error message in the machine. This message can be seen when the
     * Analyzer is used on computer cases, for example.
     *
     * @param message the message to set.
     * @return <tt>true</tt> if the computer switched to the stopping state.
     */
    boolean crash(String message);

    /**
     * Tries to pop a signal from the queue and returns it.
     * <p/>
     * Signals are stored in a FIFO queue of limited size. This method is / must
     * be called by architectures regularly to process the queue.
     *
     * @return a signal or <tt>null</tt> if the queue was empty.
     */
    Signal popSignal();

    /**
     * Get a list of all methods and their annotations of the specified object.
     * <p/>
     * The specified object can be either a {@link nl.jk_5.pumpkin.server.lua.Value}
     * or a {@link nl.jk_5.pumpkin.server.lua.network.Environment}. This is useful for
     * custom architectures, to allow providing a list of callback methods to
     * evaluated programs.
     *
     * @param value the value to get the method listing for.
     * @return the methods that can be called on the object.
     */
    Map<String, Callback> getMethods(Object value);

    /**
     * Makes the machine call a component callback.
     * <p/>
     * This is intended to be used from architectures, but may be useful in
     * other scenarios, too. It will make the machine call the method with the
     * specified name on the attached component with the specified address.
     * <p/>
     * This will perform a visibility check, ensuring the component can be seen
     * from the machine. It will also ensure that the direct call limit for
     * individual callbacks is respected.
     *
     * @param address the address of the component to call the method on.
     * @param method  the name of the method to call.
     * @param args    the list of arguments to pass to the callback.
     * @return a list of results returned by the callback, or <tt>null</tt>.
     * @throws LimitReachedException    when the called method supports direct
     *                                  calling, but the number of calls in this
     *                                  tick has exceeded the allowed limit.
     * @throws IllegalArgumentException if there is no such component.
     * @throws Exception                if the callback throws an exception.
     */
    Object[] invoke(String address, String method, Object[] args) throws Exception;

    /**
     * Makes the machine call a value callback.
     * <p/>
     * This is intended to be used from architectures, but may be useful in
     * other scenarios, too. It will make the machine call the method with the
     * specified name on the specified value.
     * <p/>
     * This will will ensure that the direct call limit for individual
     * callbacks is respected.
     *
     * @param value  the value to call the method on.
     * @param method the name of the method to call.
     * @param args   the list of arguments to pass to the callback.
     * @return a list of results returned by the callback, or <tt>null</tt>.
     * @throws LimitReachedException    when the called method supports direct
     *                                  calling, but the number of calls in this
     *                                  tick has exceeded the allowed limit.
     * @throws IllegalArgumentException if there is no such component.
     * @throws Exception                if the callback throws an exception.
     */
    Object[] invoke(Value value, String method, Object[] args) throws Exception;

    Optional<String> getLastErrorMessage();

    FileSystem getFileSystem();

    Map<String, Component> getComponents();

    Multimap<String, String> getComponentAddresses();

    void addComponent(Component component);

    void removeComponent(Component component);
}
