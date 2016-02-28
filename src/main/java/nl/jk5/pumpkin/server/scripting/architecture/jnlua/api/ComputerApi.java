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
        // Computer API, stuff that kinda belongs to os, but we don't want to
        // clutter it.
        lua().newTable();

        // Allow getting the real world time for timeouts.
        lua().pushJavaFunction(lua -> {
            lua.pushNumber(System.currentTimeMillis() / 1000.0);
            return 1;
        });
        lua().setField(-2, "realTime");

        // The time the computer has been running, as opposed to the CPU time.
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

        // Set the computer table.
        lua().setGlobal("computer");
    }
}
