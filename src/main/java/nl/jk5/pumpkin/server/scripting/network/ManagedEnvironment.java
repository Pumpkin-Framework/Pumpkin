package nl.jk5.pumpkin.server.scripting.network;

public interface ManagedEnvironment extends Environment {

    default boolean canUpdate(){
        return false;
    }

    /**
     * This is called by the host of this managed environment once per tick.
     */
    default void update(){

    }

    @Override
    default void onConnect(Node node) {

    }

    @Override
    default void onDisconnect(Node node) {

    }

    @Override
    default void onMessage(Message message) {

    }
}
