package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.LuaStateUtils;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;
import nl.jk5.pumpkin.server.scripting.component.Component;
import nl.jk5.pumpkin.server.scripting.component.Node;

import java.util.Map;
import java.util.Optional;

public class ComponentApi extends NativeLuaApi {

    public ComponentApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            synchronized (getMachine().getComponents()) {
                boolean exact;
                String filter;
                if(lua.isString(1)){
                    filter = lua.toString(1);
                }else{
                    filter = null;
                }
                if(lua.isBoolean(2)){
                    exact = lua.toBoolean(2);
                }else{
                    exact = true;
                }

                lua.newTable(0, getMachine().getComponents().size());
                getMachine().getComponents().forEach((address, name) -> {
                    if(filter != null){
                        if(exact){
                            if(!name.equals(filter)){
                                return;
                            }
                        }else{
                            if(!name.contains(filter)){
                                return;
                            }
                        }
                    }
                    lua.pushString(address);
                    lua.pushString(name);
                    lua.rawSet(-3);
                });
            }
            return 1;
        });
        lua().setField(-2, "list");

        lua().pushJavaFunction(lua -> {
            synchronized (getMachine().getComponents()) {
                String name = getMachine().getComponents().get(lua.checkString(1));
                if(name != null){
                    lua.pushString(name);
                    return 1;
                }else{
                    lua.pushNil();
                    lua.pushString("no such component");
                    return 2;
                }
            }
        });
        lua().setField(-2, "type");

        lua().pushJavaFunction(lua -> {
            String address = lua.checkString(1);
            Optional<Node> nodeOpt = getMachine().getNode().getNetwork().getNode(address);
            if(!nodeOpt.isPresent() || !(nodeOpt.get() instanceof Component)){
                lua.pushNil();
                lua.pushString("no such component");
                return 2;
            }
            Component component = (Component) nodeOpt.get();
            lua.newTable();
            getMachine().getMethods(component).forEach((name, annotation) -> {
                lua.pushString(name);
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
            Optional<Node> nodeOpt = getMachine().getNode().getNetwork().getNode(address);
            if(!nodeOpt.isPresent() || !(nodeOpt.get() instanceof Component)){
                lua.pushNil();
                lua.pushString("no such component");
                return 2;
            }
            Component component = (Component) nodeOpt.get();
            String method = lua.checkString(2);
            Map<String, Callback> methods = getMachine().getMethods(component.getHost());
            return getOwner().documentation(() -> Optional.ofNullable(methods.get(method)).map(Callback::doc).orElse(null));
        });
        lua().setField(-2, "doc");

        lua().setGlobal("component");
    }
}
