package nl.jk5.pumpkin.server.scripting.component;

import java.util.Optional;

/**
 * Interface for interacting with networks.
 * <p/>
 * Computers and components form ad-hoc "networks" when placed next to each
 * other. They allow computers to communicate with the components attached to
 * them (and nodes amongst each other) by using the network as an index
 * structure.
 * <p/>
 * There are three types of nodes:
 * <ul>
 * <li>{@link Node}, the most basic form.</li>
 * <li>{@link Component}, used to expose callbacks to user code.</li>
 * </ul>
 * <p/>
 * See <tt>Node</tt> for more details on the behavior of single nodes, and in
 * particular how nodes represented by tile entities should be added.
 * <p/>
 * Note that network access in general is <em>not</em> thread safe! Networks
 * should only be accessed from the main server thread. The exception are the
 * connector nodes, which can be used to consume or produce energy from other
 * threads.
 * <p/>
 * IMPORTANT: do *not* implement this interface yourself and create
 * instances of your own network implementation; this will lead to
 * incompatibilities with the built-in network implementation (which can only
 * merge with other networks of its own type). Always use the methods provided
 * in {@link li.cil.oc.api.Network} to create and join networks.
 */
public interface Network {
    /**
     * Adds a new node connection in the network.
     *
     * @return true if a new connection between the two nodes was added; false if
     * the connection already existed.
     * @throws IllegalArgumentException if neither node is in this network.
     */
    boolean connect(Node node);

    /**
     * Removes a node connection in the network.
     *
     * @return true if the connection was cut; false if there was none.
     * @throws IllegalArgumentException if the nodes are not in this network.
     */
    boolean disconnect(Node node);

    /**
     * Removes a node from the network.
     * <p/>
     * This should be called by nodes when they are destroyed (e.g. in
     * {@link net.minecraft.tileentity.TileEntity#invalidate()}) or unloaded
     * (e.g. in {@link net.minecraft.tileentity.TileEntity#onChunkUnload()}).
     * Removing the node can lead to one or more new networks if it was the a
     * bridge node, i.e. the only node connecting the resulting networks.
     *
     * @param node the node to remove from the network.
     * @return true if the node was removed; false if it wasn't in the network.
     */
    boolean remove(Node node);

    /**
     * Get the network node with the specified address.
     *
     * @param address the address of the node to get.
     * @return the node with that address.
     */
    Optional<Node> getNode(String address);

    /**
     * The list of all nodes in this network.
     *
     * @return the list of nodes in this network.
     */
    Iterable<Node> nodes();

    // ----------------------------------------------------------------------- //

    /**
     * Sends a message to the node with the specified address.
     * <p/>
     * If the target node with that address has a visibility of <tt>None</tt>
     * the message will <em>not</em> be delivered to that node. If the target
     * node with that address has a visibility of <tt>Neighbors</tt> and the
     * source node is not directly connected to the target the message will
     * <em>not</em> be delivered to that node.
     * <p/>
     * Messages should have a unique name to allow differentiating them when
     * handling them in a network node. For example, computers will try to parse
     * messages named <tt>computer.signal</tt> by converting the message data to
     * a signal and inject that signal into the machine, so no message not used
     * for this purpose should be named <tt>computer.signal</tt>.
     *
     * @param source the node that sends the message.
     * @param target the id of the node to send the message to.
     * @param name   the name of the message.
     * @param data   the message to send.
     * @throws IllegalArgumentException if the source node is not in this network.
     */
    void sendToAddress(Node source, String target, String name, Object... data);

    /**
     * Sends a message to all addressed nodes visible to the source node.
     * <p/>
     * Targets are determined using {@link #nodes(Node)} and additionally
     * filtered for visibility (so that invisible nodes are ignored).
     * <p/>
     * Note that messages sent this way are <em>only</em> delivered to other
     * components. The message will <em>not</em> be delivered to normal nodes.
     * <p/>
     * Messages should have a unique name to allow differentiating them when
     * handling them in a network node. For example, computers will try to parse
     * messages named <tt>computer.signal</tt> by converting the message data to
     * a signal and inject that signal into the machine, so no message not used
     * for this purpose should be named <tt>computer.signal</tt>.
     *
     * @param source the node that sends the message.
     * @param data   the message to send.
     * @throws IllegalArgumentException if the source node is not in this network.
     * @see #nodes(Node)
     * @see Component#canBeSeenFrom(Node)
     */
    void sendToVisible(Node source, String name, Object... data);
}