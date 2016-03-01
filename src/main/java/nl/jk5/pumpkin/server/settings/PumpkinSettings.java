package nl.jk5.pumpkin.server.settings;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import nl.jk5.pumpkin.server.Log;

import java.io.IOException;

public final class PumpkinSettings {

    private final String dbConn;
    private final LuaVmSettings luaVmSettings;
    private final int lobbyMappack;

    public PumpkinSettings(ConfigurationLoader<CommentedConfigurationNode> configManager) throws IOException {
        CommentedConfigurationNode config = configManager.load();

        if(config.getNode("database", "connection-string").isVirtual()){
            config.getNode("database", "connection-string")
                    .setComment("The JDBC connection string for the database")
                    .setValue("jdbc:postgresql://HOST/DATABASE?user=USER&password=PASSWORD");
        }

        if(config.getNode("lobby-mappack").isVirtual()){
            config.getNode("lobby-mappack")
                    .setComment("The mappack to use for the lobby. The lobby is the map where players will enter if they join the server for the first time")
                    .setValue(-1);
        }

        dbConn = config.getNode("database", "connection-string")
                .setComment("The JDBC connection string for the database")
                .getString();

        lobbyMappack = config.getNode("lobby-mappack").getInt();

        luaVmSettings = new LuaVmSettings(config.getNode("lua-vm").setComment("Configuration for the Lua VM"));

        try{
            configManager.save(config);
        }catch(IOException e){
            Log.error("Could not save config", e);
        }
    }

    public String getDatabaseConnectionString() {
        return dbConn;
    }

    public LuaVmSettings getLuaVmSettings() {
        return luaVmSettings;
    }

    public int getLobbyMappack() {
        return lobbyMappack;
    }
}
