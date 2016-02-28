package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;

public class SystemApi extends NativeLuaApi {

    public SystemApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        // Until we get to ingame screens we log to Java's stdout.
        lua().pushJavaFunction(new JavaFunction() {
            @Override
            public int invoke(LuaState lua) {
                String ret = "";
                for (int i = 1; i <= lua.getTop(); i++) {
                    LuaType type = lua.type(i);
                    switch (type) {
                        case NIL:
                            ret += "nil";
                            break;
                        case BOOLEAN:
                            ret += lua.toBoolean(i);
                            break;
                        case NUMBER:
                            ret += lua.toNumber(i);
                            break;
                        case STRING:
                            ret += lua.toString(i);
                            break;
                        case TABLE:
                            ret += "table";
                            break;
                        case FUNCTION:
                            ret += "function";
                            break;
                        case THREAD:
                            ret += "thread";
                            break;
                        case LIGHTUSERDATA:
                        case USERDATA:
                            ret += "userdata";
                            break;
                    }
                    ret += "  ";
                }
                Log.info(ret.trim());
                return 0;
            }
        });
        lua().setGlobal("print");

        // Create system table, avoid magic global non-tables.
        lua().newTable();

        // Whether bytecode may be loaded directly.
        lua().pushJavaFunction(lua -> {
            // TODO: 28-2-16 Make this configurable
            //lua.pushBoolean(Settings.lua.allowBytecode);
            lua.pushBoolean(false);
            return 1;
        });
        lua().setField(-2, "allowBytecode");

        // How long programs may run without yielding before we stop them.
        lua().pushJavaFunction(lua -> {
            // TODO: 28-2-16 make this configurable
            //lua.pushNumber(Settings.lua.timeout);
            lua.pushNumber(5);
            return 1;
        });
        lua().setField(-2, "timeout");

        lua().setGlobal("system");
    }
}
