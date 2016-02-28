package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import gnu.trove.list.array.TCharArrayList;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;


public class UnicodeApi extends NativeLuaApi {

    public UnicodeApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            int top = lua.getTop();
            TCharArrayList list = new TCharArrayList();
            for(int i = 1; i <= top; i++){
                list.add((char) lua.checkInteger(i));
            }
            lua.pushString(String.valueOf(list.toArray()));
            return 1;
        });
        lua().setField(-2, "char");

        lua().pushJavaFunction(lua -> {
            lua.pushInteger(lua.checkString(1).length());
            return 1;
        });
        lua().setField(-2, "len");

        lua().pushJavaFunction(lua -> {
            lua.pushString(lua.checkString(1).toLowerCase());
            return 1;
        });
        lua().setField(-2, "lower");

        lua().pushJavaFunction(lua -> {
            lua.pushString(lua.checkString(1).toUpperCase());
            return 1;
        });
        lua().setField(-2, "upper");

        lua().pushJavaFunction(lua -> {
            lua.pushString(reverse(lua.checkString(1)));
            return 1;
        });
        lua().setField(-2, "reverse");

        lua().pushJavaFunction(lua -> {
            String str = lua.checkString(1);
            int s = lua.checkInteger(2);
            int start = Math.max(0, s < 0 ? str.length() + s : s - 1);
            int end;
            if(lua.getTop() > 2){
                int e = lua.checkInteger(3);
                end = Math.min(str.length(), e < 0 ? str.length() + e + 1 : e);
            }else{
                end = str.length();
            }
            if(end <= start){
                lua.pushString("");
            }else{
                lua.pushString(str.substring(start, end));
            }
            return 1;
        });
        lua().setField(-2, "sub");

        lua().setGlobal("unicode");
    }

    public static String reverse(String source) {
        int i, len = source.length();
        StringBuilder dest = new StringBuilder(len);

        for (i = (len - 1); i >= 0; i--){
            dest.append(source.charAt(i));
        }

        return dest.toString();
    }
}