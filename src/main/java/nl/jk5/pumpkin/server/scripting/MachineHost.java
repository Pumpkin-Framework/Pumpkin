package nl.jk5.pumpkin.server.scripting;

import nl.jk5.pumpkin.server.scripting.component.Node;

public interface MachineHost {

    Machine getMachine();

    void onMachineConnect(Node node);

    void onMachineDisconnect(Node node);
}
