package nl.jk5.pumpkin.server.scripting;

import nl.jk5.pumpkin.server.scripting.builder.MutableNode;
import nl.jk5.pumpkin.server.scripting.builder.SimpleComponent;
import nl.jk5.pumpkin.server.scripting.component.Node;

import java.util.*;

public class NetworkImpl implements nl.jk5.pumpkin.server.scripting.component.Network {

    private final Set<Node> nodes = new HashSet<>();
    private final Map<String, Node> nodeMap = new HashMap<>();

    public NetworkImpl() {
    }

    @Override
    public boolean connect(Node node) {
        return this.nodes.add(node);
    }

    @Override
    public boolean disconnect(Node node) {
        return this.nodes.remove(node);
    }

    @Override
    public boolean remove(Node node) {
        return this.nodes.remove(node);
    }

    @Override
    public Optional<Node> getNode(String address) {
        return null;
    }

    @Override
    public Iterable<Node> nodes() {
        return null;
    }

    @Override
    public void sendToAddress(Node source, String target, String name, Object... data) {

    }

    @Override
    public void sendToVisible(Node source, String name, Object... data) {

    }

    private boolean add(MutableNode addedNode){
        if(addedNode.getNetwork() == null){
            ((MutableNode) addedNode).onConnect(node);
            val = newNode = addNew(addedNode);
            // TODO: 28-2-16 Connect it
        }else{

        }
    }
}
