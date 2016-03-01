package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import com.google.common.collect.ImmutableMap;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.scripting.ArgumentsImpl;
import nl.jk5.pumpkin.server.scripting.LuaStateUtils;
import nl.jk5.pumpkin.server.scripting.Value;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;

public class UserdataApi extends NativeLuaApi {

    public UserdataApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            Object[] args = LuaStateUtils.toSimpleJavaObjects(lua, 2);
            return getOwner().invoke(() -> new Object[]{value.apply(getMachine(), new ArgumentsImpl(args))});
        });
        lua().setField(-2, "apply");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            Object[] args = LuaStateUtils.toSimpleJavaObjects(lua, 2);
            return getOwner().invoke(() -> {
                value.unapply(getMachine(), new ArgumentsImpl(args));
                return null;
            });
        });
        lua().setField(-2, "unapply");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            Object[] args = LuaStateUtils.toSimpleJavaObjects(lua, 2);
            return getOwner().invoke(() -> value.call(getMachine(), new ArgumentsImpl(args)));
        });
        lua().setField(-2, "call");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            try{
                value.dispose(getMachine());
            }catch (Throwable t){
                Log.warn("Error in dispose method of userdata of type " + value.getClass().getName(), t);
            }
            return 0;
        });
        lua().setField(-2, "dispose");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            ImmutableMap.Builder<String, Boolean> ret = ImmutableMap.builder();
            getMachine().getMethods(value).forEach((name, annotation) -> ret.put(name, annotation.direct()));
            LuaStateUtils.pushValue(lua, ret.build(), getMachine());
            return 1;
        });
        lua().setField(-2, "methods");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            String method = lua.checkString(2);
            Object[] args = LuaStateUtils.toSimpleJavaObjects(lua, 3);
            return getOwner().invoke(() -> getMachine().invoke(value, method, args));
        });
        lua().setField(-2, "invoke");

        lua().pushJavaFunction(lua -> {
            Value value = (Value) lua.toJavaObjectRaw(1);
            String method = lua.checkString(2);
            return getOwner().documentation(() -> getMachine().getMethods(value).get(method).doc());
        });
        lua().setField(-2, "doc");

        lua().setGlobal("userdata");
    }
}
