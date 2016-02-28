package nl.jk5.pumpkin.server.scripting.builder;

import com.google.common.base.Objects;
import nl.jk5.pumpkin.api.mappack.Map;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.scripting.component.Environment;
import nl.jk5.pumpkin.server.scripting.component.Network;
import nl.jk5.pumpkin.server.scripting.component.Node;

public class MutableNode implements Node {

    private final Environment host;
    private final Map map;

    private String address;
    private Network network;

    public MutableNode(Environment host, Map map) {
        this.host = host;
        this.map = map;
    }

    public void onConnect(Node node){
        try{
            host.onConnect(node);
        }catch(Throwable e){
            Log.warn("A component of type " + host.getClass().getName() + " threw an error while being connected to the component network", e);
        }
    }

    public void onDisconnect(Node node){
        try{
            host.onDisconnect(node);
        }catch(Throwable e){
            Log.warn("A component of type " + host.getClass().getName() + " threw an error while being disconnected from the component network", e);
        }
    }

    @Override
    public Environment getHost() {
        return this.host;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public void sendToAddress(String target, String name, Object... data) {
        if(this.network != null){
            this.network.sendToAddress(this, target, name, data);
        }
    }

    @Override
    public void sendToVisible(String name, Object... data) {
        if(this.network != null){
            this.network.sendToVisible(this, name, data);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("address", address)
                .add("host", host)
                .toString();
    }
}
