package nl.jk5.pumpkin.server.map.stat.type;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import nl.jk5.pumpkin.api.mappack.game.stat.StatConfig;
import nl.jk5.pumpkin.api.mappack.game.stat.StatType;
import nl.jk5.pumpkin.api.mappack.game.stat.StatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.MapStatManager;
import nl.jk5.pumpkin.server.map.stat.type.factory.GameDoneStatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.type.factory.GameRunningStatTypeFactory;
import nl.jk5.pumpkin.server.map.stat.type.factory.ManualStatTypeFactory;

import java.util.Map;
import java.util.Optional;

public class StatTypeRegistry {

    private final Map<String, StatTypeFactory> types;

    public StatTypeRegistry(MapStatManager manager) {
        types = ImmutableMap.<String, StatTypeFactory>builder()
                .put("game_running", new GameRunningStatTypeFactory())
                .put("game_done", new GameDoneStatTypeFactory())
                .put("manual", new ManualStatTypeFactory())
                .build();
    }

    public Optional<StatType> create(StatConfig config){
        StatTypeFactory<?> factory = types.get(config.getType());
        if(factory == null){
            return Optional.empty();
        }
        JsonParser parser = new JsonParser();
        StatType type = factory.create(config.getName(), parser.parse(config.getArguments()).getAsJsonObject());
        if(type == null){
            return Optional.empty();
        }
        return Optional.of(type);
    }
}
