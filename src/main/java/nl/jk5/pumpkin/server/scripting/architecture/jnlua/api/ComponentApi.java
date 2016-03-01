package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.LuaStateUtils;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;
import nl.jk5.pumpkin.server.scripting.component.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComponentApi extends NativeLuaApi {

    public ComponentApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            synchronized (getMachine().getComponents()) {
                Collection<Component> components;
                if(lua.isString(1)){
                    components = getMachine().getComponentAddresses().get(lua.checkString(1)).stream().map(n -> getMachine().getComponents().get(n)).collect(Collectors.toList());
                }else{
                    components = getMachine().getComponents().values();
                }
                lua.newTable(0, getMachine().getComponents().size());
                components.forEach(c -> {
                    lua.pushString(c.address());
                    lua.pushString(c.type());
                    lua.rawSet(-3);
                });
                return 1;
            }
        });
        lua().setField(-2, "list");

        lua().pushJavaFunction(lua -> {
            String name = lua.checkString(1);
            Component component = getMachine().getComponents().get(name);
            if(component == null){
                lua.pushNil();
                lua.pushString("no such component");
                return 2;
            }else{
                lua.pushString(component.type());
                return 1;
            }
        });
        lua().setField(-2, "type");

        lua().pushJavaFunction(lua -> {
            String name = lua.checkString(1);
            Component component = getMachine().getComponents().get(name);
            if(component == null){
                lua.pushNil();
                lua.pushString("no such component");
                return 2;
            }else{
                lua.newTable();
                getMachine().getMethods(component).forEach((n, annotation) -> {
                    lua.pushString(n);
                    lua.newTable();
                    lua.pushBoolean(annotation.direct());
                    lua.setField(-2, "direct");
                    lua.pushBoolean(annotation.getter());
                    lua.setField(-2, "getter");
                    lua.pushBoolean(annotation.setter());
                    lua.setField(-2, "setter");
                    lua.rawSet(-3);
                });
                return 1;
            }
        });
        lua().setField(-2, "methods");

        lua().pushJavaFunction(lua -> {
            String address = lua.checkString(1);
            String method = lua.checkString(2);
            Object[] args = LuaStateUtils.toSimpleJavaObjects(lua, 3);
            return getOwner().invoke(() -> getMachine().invoke(address, method, args));
        });
        lua().setField(-2, "invoke");

        lua().pushJavaFunction(lua -> {
            String address = lua.checkString(1);
            String method = lua.checkString(2);
            Component component = getMachine().getComponents().get(address);
            if(component == null){
                lua.pushNil();
                lua.pushString("no such component");
                return 2;
            }
            Map<String, Callback> methods = getMachine().getMethods(component);
            return getOwner().documentation(() -> Optional.ofNullable(methods.get(method)).map(Callback::doc).orElse(null));
        });
        lua().setField(-2, "doc");

        lua().setGlobal("component");
    }
}
