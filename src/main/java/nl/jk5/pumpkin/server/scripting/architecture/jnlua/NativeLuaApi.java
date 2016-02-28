package nl.jk5.pumpkin.server.scripting.architecture.jnlua;

import com.naef.jnlua.LuaState;
import nl.jk5.pumpkin.server.scripting.architecture.ArchitectureApi;

public abstract class NativeLuaApi extends ArchitectureApi {

    private final JNLuaArchitecture owner;

    public NativeLuaApi(JNLuaArchitecture owner) {
        super(owner.getMachine());
        this.owner = owner;
    }

    protected LuaState lua() {
        return owner.lua;
    }

    public JNLuaArchitecture getOwner() {
        return owner;
    }
}
