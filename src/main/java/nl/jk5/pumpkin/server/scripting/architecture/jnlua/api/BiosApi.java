package nl.jk5.pumpkin.server.scripting.architecture.jnlua.api;

import nl.jk5.pumpkin.server.scripting.architecture.jnlua.JNLuaArchitecture;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.NativeLuaApi;

import java.io.*;

public class BiosApi extends NativeLuaApi {

    public BiosApi(JNLuaArchitecture owner) {
        super(owner);
    }

    @Override
    public void initialize() {
        lua().newTable();

        lua().pushJavaFunction(lua -> {
            try(
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/jk-5/development/pumpkin/pumpkin/src/main/resources/assets/pumpkin/lua/kernel/bios.lua"))));
            ){
                String code = "";
                String line;
                while((line = br.readLine()) != null){
                    code += line + "\n";
                }
                lua.pushString(code);
                return 1;
            }catch (IOException e){
                return 0;
            }
        });
        lua().setField(-2, "load");

        lua().setGlobal("bios");
    }
}
