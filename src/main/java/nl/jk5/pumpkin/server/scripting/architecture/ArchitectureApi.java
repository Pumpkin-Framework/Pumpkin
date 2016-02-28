package nl.jk5.pumpkin.server.scripting.architecture;

import nl.jk5.pumpkin.server.scripting.Machine;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public abstract class ArchitectureApi {

    private final Machine machine;

    public ArchitectureApi(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }

    public abstract void initialize();
}
