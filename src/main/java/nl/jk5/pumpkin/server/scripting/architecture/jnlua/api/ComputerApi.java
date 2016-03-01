package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import nl.jk5.pumpkin.server.scripting.LuaStateUtils;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;

public class ComputerApi extends NativeLuaApi {

    public ComputerApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            lua.pushNumber(System.currentTimeMillis() / 1000.0);
            return 1;
        });
        lua().setField(-2, "realTime");

        lua().pushJavaFunction(lua -> {
            lua.pushNumber(getMachine().upTime());
            return 1;
        });
        lua().setField(-2, "uptime");

        lua().pushJavaFunction(lua -> {
            lua.pushInteger(Math.min(lua.getFreeMemory(), (lua.getTotalMemory() - getOwner().getKernelMemory())));
            return 1;
        });
        lua().setField(-2, "freeMemory");

        lua().pushJavaFunction(lua -> {
            lua.pushInteger(lua.getTotalMemory() - getOwner().getKernelMemory());
            return 1;
        });
        lua().setField(-2, "totalMemory");

        lua().pushJavaFunction(lua -> {
            lua.pushBoolean(getMachine().signal(lua.checkString(1), LuaStateUtils.toSimpleJavaObjects(lua, 2)));
            return 1;
        });
        lua().setField(-2, "pushSignal");

        lua().pushJavaFunction(lua -> {
            String tmpAddress = getMachine().tmpAddress();
            if(tmpAddress == null){
                lua.pushNil();
            }else{
                lua.pushString(tmpAddress);
            }
            return 1;
        });
        lua().setField(-2, "tmpAddress");

        lua().setGlobal("computer");
    }
}
