package nl.jk5.pumpkin.server.scripting.component.impl;

import nl.jk5.pumpkin.server.scripting.Network;
import nl.jk5.pumpkin.server.scripting.component.Component;
import nl.jk5.pumpkin.server.scripting.component.ManagedEnvironment;
import nl.jk5.pumpkin.server.scripting.component.Node;

public class TestComponent implements ManagedEnvironment {

    private final Component node = Network.newNode(this)
            .withComponent("test")
            .create();

    public TestComponent() {
    }

    @Override
    public void update() {

    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public void onConnect(Node node) {

    }

    @Override
    public void onDisconnect(Node node) {

    }
}
