package nl.jk5.pumpkin.server.scripting.component;

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
    Environment getHost();

    /**
     * The address of the node, so that it can be found in the network.
     * <p/>
     * This is used by the network manager when a node is added to a network to
     * assign it a unique address, if it doesn't already have one. Nodes must not
     * use custom addresses, only those assigned by the network. The only option
     * they have is to *not* have an address, which can be useful for "dummy"
     * nodes, such as cables. In that case they may ignore the address being set.
     */
    String getAddress();

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
    Network getNetwork();

    /**
     * Send a message to a node with the specified address.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will do nothing.
     *
     * @param target the address of the node to send the message to.
     * @param name   the name of the message.
     * @param data   the data to pass along with the message.
     */
    default void sendToAddress(String target, String name, Object... data){
        getNetwork().sendToAddress(this, target, name, data);
    }

    /**
     * Send a message to all nodes visible from this node.
     * <p/>
     * If this node is not in a network, i.e. <tt>network</tt> is <tt>null</tt>,
     * this will do nothing.
     *
     * @param name the name of the message.
     * @param data the data to pass along with the message.
     */
    default void sendToVisible(String name, Object... data){
        getNetwork().sendToVisible(this, name, data);
    }

    default void connect(){
        getNetwork().connect(this);
    }

    default void disconnect(){
        getNetwork().disconnect(this);
    }

    default void remove(){
        getNetwork().remove(this);
    }
}
