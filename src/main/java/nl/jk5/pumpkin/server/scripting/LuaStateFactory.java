package nl.jk5.pumpkin.server.scripting;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NativeSupport;
import nl.jk5.pumpkin.server.Log;

import java.util.Optional;
import java.util.Random;

public final class LuaStateFactory {

    static {
        NativeSupport.getInstance().setLoader(() -> System.load("/home/jk-5/development/pumpkin/jnlua/src/main/resources/libjnlua52.so"));
    }

    public static Optional<LuaState> create(){
        try{
            LuaState state = new LuaState(); // TODO: 28-2-16 Allow max memory to be configured

            state.openLib(LuaState.Library.BASE);
            state.openLib(LuaState.Library.BIT32);
            state.openLib(LuaState.Library.COROUTINE);
            state.openLib(LuaState.Library.DEBUG);
            state.openLib(LuaState.Library.MATH);
            state.openLib(LuaState.Library.STRING);
            state.openLib(LuaState.Library.TABLE);
            state.pop(7);

            //if(!Settings.lua.disableLocaleChanging){
            //    state.openLib(LuaState.Library.OS);
            //    state.getField(-1, "setlocale");
            //    state.pushString("C");
            //    state.call(1, 0);
            //    state.pop(1);
            //}

            state.newTable();
            state.setGlobal("os");

            // Kill lua <--> java compat functions
            state.pushNil();
            state.setGlobal("unpack");

            state.pushNil();
            state.setGlobal("loadstring");

            state.getGlobal("math");
            state.pushNil();
            state.setField(-2, "log10");
            state.pop(1);

            state.getGlobal("table");
            state.pushNil();
            state.setField(-2, "maxn");
            state.pop(1);

            // Remove script loading functions that will be able to break out of the sandbox
            state.pushNil();
            state.setGlobal("dofile");
            state.pushNil();
            state.setGlobal("loadfile");

            state.getGlobal("math");

            // We give each Lua state it's own randomizer, since otherwise they'd
            // use the normal rand() from C. Which can be terrible, and isn't
            // necessarily thread-safe.
            final Random random = new Random();
            state.pushJavaFunction(lua -> {
                double r = random.nextDouble();
                int top = lua.getTop();
                if (top == 0) {
                    lua.pushNumber(r);
                    return 1;
                } else if (top == 1) {
                    double u = lua.checkNumber(1);
                    lua.checkArg(1, 1 <= u, "interval is empty");
                    lua.pushNumber(Math.floor(r * u) + 1);
                    return 1;
                } else if (top == 2) {
                    double l = lua.checkNumber(1);
                    double u = lua.checkNumber(2);
                    lua.checkArg(2, l <= u, "interval is empty");
                    lua.pushNumber(Math.floor(r * (u - l + 1)) + l);
                    return 1;
                } else {
                    throw new IllegalArgumentException("wrong number of arguments");
                }
            });
            state.setField(-2, "random");

            state.pushJavaFunction(lua -> {
                random.setSeed((long) lua.checkNumber(1));
                return 0;
            });
            state.setField(-2, "randomseed");
            state.pop(1);

            return Optional.of(state);
        }catch(UnsatisfiedLinkError e){
            Log.error("Could not load native lua libraries: " + e.getMessage());
            return Optional.empty();
        }
    }
}
