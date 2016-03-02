package nl.jk5.pumpkin.server.map.stat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import nl.jk5.pumpkin.api.mappack.MapWorld;
import nl.jk5.pumpkin.api.mappack.game.stat.StatManager;
import nl.jk5.pumpkin.api.mappack.game.stat.StatType;
import nl.jk5.pumpkin.server.map.DefaultMap;
import nl.jk5.pumpkin.server.map.stat.type.StatTypeRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapStatManager implements StatManager {

    private final DefaultMap map;
    private final StatTypeRegistry typeRegistry = new StatTypeRegistry(this);

    private final List<StatType> stats;
    private final Map<Integer, StatType> configs;
    private final Map<String, StatType> namedStats;
    private final Multimap<MapWorld, StatEmitter> statEmitters = HashMultimap.create();

    public MapStatManager(DefaultMap map) {
        this.map = map;

        ImmutableList.Builder<StatType> builder = ImmutableList.builder();
        ImmutableMap.Builder<Integer, StatType> configsBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, StatType> namesBuilder = ImmutableMap.builder();
        map.getMappack().getStats().forEach(s -> {
            Optional<StatType> type = this.typeRegistry.create(s);
            if(type.isPresent()){
                builder.add(type.get());
                configsBuilder.put(s.getId(), type.get());
                namesBuilder.put(s.getName(), type.get());
            }
        });
        this.stats = builder.build();
        this.configs = configsBuilder.build();
        this.namedStats = namesBuilder.build();
    }

    @Override
    public Collection<StatEmitter> getStatEmitters(MapWorld world) {
        return statEmitters.get(world);
    }

    @Override
    public void addStatEmitter(StatEmitter emitter){
        this.statEmitters.put(emitter.getWorld(), emitter);

        emitter.save();
    }

    public DefaultMap getMap() {
        return map;
    }

    @Override
    public <T extends StatType> Collection<T> getAllOf(Class<T> type){
        //noinspection unchecked
        return this.stats.stream().filter(type::isInstance).map(t -> (T) t).collect(Collectors.<T>toList());
    }

    @Override
    public <T extends StatType> void with(Class<T> type, Consumer<T> method){
        //noinspection unchecked
        this.stats.stream().filter(type::isInstance).map(t -> (T) t).forEach(method);
    }

    @Override
    public StatType byName(String name) {
        return this.namedStats.get(name);
    }

    public void onWorldAdded(MapWorld world) {
        world.getConfig().getStatEmitters().forEach(s -> {
            StatEmitter emitter = StatEmitter.from(world, s);

            if(s.getStat() != null){
                StatType type = this.configs.get(s.getStat().getId());
                type.registerListener(emitter);
            }
            this.statEmitters.put(world, emitter);
        });
    }
}
