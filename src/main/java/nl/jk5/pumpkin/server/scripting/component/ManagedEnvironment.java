package nl.jk5.pumpkin.server.scripting.component;

public interface ManagedEnvironment extends Environment {

    /**
     * This is called by the host of this managed environment once per tick.
     */
    void update();
}
