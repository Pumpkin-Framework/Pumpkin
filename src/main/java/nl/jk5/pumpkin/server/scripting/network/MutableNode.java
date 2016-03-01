package nl.jk5.pumpkin.server.scripting.network;

import com.google.common.base.Objects;
import nl.jk5.pumpkin.server.Log;

public class MutableNode implements Node {

    private final Environment host;

    private Network network;
    private String address;

    public MutableNode(Environment host) {
        this.host = host;
    }

    @Override
    public Environment host() {
        return this.host;
    }

    @Override
    public String address() {
        return this.address;
    }

    @Override
    public Network network() {
        return this.network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    ////////////////////////////////////////////////

    public void onConnect(Node node){
        try{
            host.onConnect(node);
        }catch (Throwable e){
            Log.warn("A component of type '" + host.getClass().getName() + "' threw an error while being connected to the component network.", e);
        }
    }

    public void onDisconnect(Node node){
        try{
            host.onDisconnect(node);
        }catch (Throwable e){
            Log.warn("A component of type '" + host.getClass().getName() + "' threw an error while being disconnected from the component network.", e);
        }
    }

    ////////////////////////////////////////////////


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("host", host)
                .add("address", address)
                .toString();
    }

    @Override
    public void sendToAddress(String target, String name, Object... data) {
        if(network != null){
            network.sendToAddress(this, target, name, data);
        }
    }

    @Override
    public void sendToAll(String name, Object... data) {
        if(network != null){
            network.sendToAll(this, name, data);
        }
    }
}
