package nl.jk5.pumpkin.server.scripting;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import nl.jk5.pumpkin.server.Log;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.*;

@NonnullByDefault
public final class LuaStateUtils {

    private LuaStateUtils() {
    }

    public static void pushValue(LuaState state, @Nullable Object value, Context ctx){
        pushValue(state, value, new IdentityHashMap<>(), ctx);
    }

    private static void pushValue(LuaState lua, @Nullable final Object value, final IdentityHashMap<Object, Integer> memo, final Context ctx){
        boolean recursive = memo.size() > 0;
        int oldTop = lua.getTop();
        if(memo.containsKey(value)){
            lua.pushValue(memo.get(value));
        }else{
            if(value == null){
                lua.pushNil();
            }else if(value instanceof Boolean){
                lua.pushBoolean((Boolean) value);
            }else if(value instanceof Byte){
                lua.pushNumber((Byte) value);
            }else if(value instanceof Character){
                lua.pushString(String.valueOf(((Character) value).charValue()));
            }else if(value instanceof Short){
                lua.pushNumber((Short) value);
            }else if(value instanceof Integer){
                lua.pushNumber((Integer) value);
            }else if(value instanceof Long){
                lua.pushNumber((Long) value);
            }else if(value instanceof Float){
                lua.pushNumber((Float) value);
            }else if(value instanceof Double){
                lua.pushNumber((Double) value);
            }else if(value instanceof String){
                lua.pushString((String) value);
            }else if(value instanceof byte[]){
                lua.pushByteArray((byte[]) value);
            }else if(value instanceof Object[]){
                pushList(lua, Arrays.asList(((Object[]) value)), memo, ctx);
            }else if(value instanceof Value){
                lua.pushJavaObjectRaw(value);
            }else if(value instanceof List<?>){
                pushList(lua, ((List<?>) value), memo, ctx);
            }else if(value instanceof Map<?, ?>){
                pushTable(lua, ((Map<?, ?>) value), memo, ctx);
            }else if(value instanceof JavaFunction){
                lua.pushJavaFunction(((JavaFunction) value));
            }else{
                Log.warn("Tried to push an unsupported value of type " + value.getClass().getName() + " to lua");
                lua.pushNil();
            }
            // Remove values kept on the stack for memoization if this is the
            // original call (not a recursive one, where we might need the memo
            // info even after returning).
            if(!recursive){
                lua.setTop(oldTop + 1);
            }
        }
    }

    public static void pushList(LuaState lua, List<?> list, IdentityHashMap<Object, Integer> memo, Context ctx){
        lua.newTable();
        int tableIndex = lua.getTop();
        memo.put(list, tableIndex);
        int count = 0;
        for(int i = 0; i < list.size(); i++){
            pushValue(lua, list.get(i), memo, ctx);
            lua.rawSet(tableIndex, i + 1);
            count ++;
        }
        // Bring table back to top (in case memo values were pushed).
        lua.pushValue(tableIndex);
        lua.pushString("n");
        lua.pushInteger(count);
        lua.rawSet(-3);
    }

    public static void pushTable(LuaState lua, Map<?, ?> map, Context ctx){
        pushTable(lua, map, new IdentityHashMap<Object, Integer>(), ctx);
    }

    public static void pushTable(LuaState lua, Map<?, ?> map, IdentityHashMap<Object, Integer> memo, Context ctx){
        lua.newTable(0, map.size());
        int tableIndex = lua.getTop();
        memo.put(map, tableIndex);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if(entry.getKey() != null){
                pushValue(lua, entry.getKey(), memo, ctx);
                int keyIndex = lua.getTop();
                pushValue(lua, entry.getValue(), memo, ctx);
                // Bring key to front, in case of memo from value push.
                // Cannot actually move because that might shift memo info.
                lua.pushValue(keyIndex);
                lua.insert(-2);
                lua.setTable(tableIndex);
            }
        }

        // Bring table back to top (in case memo values were pushed).
        lua.pushValue(tableIndex);
    }

    public static Object toSimpleJavaObject(LuaState lua, int index){
        switch (lua.type(index)){
            case BOOLEAN: return lua.toBoolean(index);
            case NUMBER: return lua.toNumberX(index);
            case STRING: return lua.toByteArray(index);
            case TABLE: return lua.toJavaObject(index, Map.class);
            case USERDATA: return lua.toJavaObjectRaw(index);
            default: return null;
        }
    }

    public static Object[] toSimpleJavaObjects(LuaState lua, int start){
        List<Object> ret = new ArrayList<>();
        for(int i = start; i <= lua.getTop(); i++){
            ret.add(toSimpleJavaObject(lua, i));
        }
        return ret.toArray(new Object[ret.size()]);
    }
}
