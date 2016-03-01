package nl.jk5.pumpkin.server.scripting.network;

import java.util.Collections;

/**
 * A single node in a {@link Network}.
 * <p/>
 * All nodes in a network have a unique address; the network will generate a
 * unique address and assign it to new nodes.
 * <p/>
 * Per default there are two kinds of nodes: tile entities and items.
 * <p/>
 * Items will usually only have nodes when in containers, such as a computer or
 * disk drive. Otherwise you'll have to connect/disconnect them manually as
 * desired.
 * <p/>
 * All other kinds of nodes you may come up with will also have to be
 * handled manually.
 * <p/>
 * Items have to be handled by a corresponding {@link li.cil.oc.api.driver.Item}.
 * Existing blocks may be interfaced with the adapter block if a
 * {@link li.cil.oc.api.driver.Block} exists that supports the block.
 * <p/>
 * <em>Important</em>: like the <tt>Network</tt> interface you must not create
 * your own implementations of this interface. Use the factory methods in the
 * network API to create new node instances and store them in your environment.
 *
 * @see Component
 */
public interface Node {
    /**
     * The environment hosting this node.
     * <p/>
     * For blocks whose tile entities implement {@link Environment} this will
     * be the tile entity. For all other implementations this will be a managed
     * environment.
     */
    Environment host();

    /**
     * The address of the node, so that it can be found in the network.
     * <p/>
     * This is used by the network manager when a node is added to a network to
     * assign it a unique address, if it doesn't already have one. Nodes must not
     * use custom addresses, only those assigned by the network. The only option
     * they have is to *not* have an address, which can be useful for "dummy"
     * nodes, such as cables. In that case they may ignore the address being set.
     */
    String address();

    /**
     * The network this node is currently in.
     * <p/>
     * Note that valid nodes should never return `None` here. When created a node
     * should immediately be added to a network, after being removed from its
     * network a node should be considered invalid.
     * <p/>
     * This will always be set automatically by the network manager. Do not
     * change this value and do not return anything that it wasn't set to.
     */
    Network network();

    // ----------------------------------------------------------------------- //

    /**
     * Get the list of nodes reachable from this node, based on their
     * {@link #reachability()}.
     * <p/>
     * This is a shortcut for <tt>node.network.nodes(node)</tt>.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this returns an empty list.
     *
     * @return the list of nodes reachable from this node.
     */
    default Iterable<Node> reachableNodes(){
        if(this.network() == null){
            return Collections.emptyList();
        }
        return network().nodes(this);
    }

    // ----------------------------------------------------------------------- //

    /**
     * Connects the specified node to this node.
     * <p/>
     * This is a shortcut for <tt>node.network.connect(node, other)</tt>.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will throw an exception.
     *
     * @throws NullPointerException if <tt>network</tt> is <tt>null</tt>.
     */
    default void connect(){
        if(network() != null){
            network().connect(this);
        }else{
            throw new NullPointerException("network");
        }
    }

    /**
     * Disconnects the specified node from this node.
     * <p/>
     * This is a shortcut for <tt>node.network.disconnect(node, other)</tt>.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will do nothing.
     *
     * @throws NullPointerException if <tt>network</tt> is <tt>null</tt>.
     */
    default void disconnect(){
        if(network() != null){
            network().disconnect(this);
        }
    }

    /**
     * Removes this node from its network.
     * <p/>
     * This is a shortcut for <tt>node.network.remove(node)</tt>.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will do nothing.
     */
    default void remove(){
        if(network() != null){
            network().remove(this);
        }
    }

    // ----------------------------------------------------------------------- //

    /**
     * Send a message to a node with the specified address.
     * <p/>
     * This is a shortcut for <tt>node.network.sendToAddress(node, ...)</tt>.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will do nothing.
     *
     * @param target the address of the node to send the message to.
     * @param name   the name of the message.
     * @param data   the data to pass along with the message.
     */
    void sendToAddress(String target, String name, Object... data);

    void sendToAll(String name, Object... data);
}