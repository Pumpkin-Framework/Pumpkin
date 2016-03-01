package nl.jk5.pumpkin.server.mappack.game;

import nl.jk5.pumpkin.api.mappack.game.Game;
import nl.jk5.pumpkin.api.mappack.game.GameStartResult;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.mappack.DefaultMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MapGame implements Game {

    private final DefaultMap map;

    private boolean starting = false;
    private boolean running = false;

    private Task startTask;

    public MapGame(DefaultMap map) {
        this.map = map;
    }

    @Override
    public GameStartResult start() {
        if(this.running){
            return GameStartResult.failed(Text.of(TextColors.RED, "A game is already running"));
        }
        if(this.starting){
            return GameStartResult.failed(Text.of(TextColors.RED, "A game is already starting"));
        }
        if(map.getPlayers().size() == 0){
            return GameStartResult.failed(Text.of(TextColors.RED, "There are no players in this map"));
        }

        this.starting = true;

        AtomicInteger secondsLeft = new AtomicInteger(10);
        this.startTask = Sponge.getScheduler().createTaskBuilder()
                .async()
                .interval(1, TimeUnit.SECONDS)
                .execute(() -> {
                    int seconds = secondsLeft.getAndDecrement();
                    if(seconds == 0){
                        doStart();
                        return;
                    }
                    Title.Builder builder = Title.builder().stay(0).fadeOut(30).fadeIn(0);
                    if(seconds == 3){
                        builder.title(Text.of(TextColors.YELLOW, seconds));
                    }else if(seconds == 2){
                        builder.title(Text.of(TextColors.GOLD, seconds));
                    }else if(seconds == 1){
                        builder.title(Text.of(TextColors.RED, seconds));
                    }else{
                        builder.title(Text.of(TextColors.WHITE, seconds));
                    }
                    map.getPlayers().forEach(p -> p.sendTitle(builder.build()));
                }).submit(Pumpkin.instance());

        return GameStartResult.success();
    }

    private void doStart(){
        this.startTask.cancel();
        this.starting = false;
        this.running = true;

        Title startTitle = Title.builder().stay(40).fadeIn(0).fadeOut(20).title(Text.of(TextColors.GREEN, "GO!")).build();
        map.getPlayers().forEach(p -> p.sendTitle(startTitle));

        this.map.getMachine().signal("game_start");
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isInActiveGame(Player player) {
        if(!this.running){
            return false;
        }
        // TODO: 1-3-16 Exclude spectators
        return this.map.getPlayers().contains(player);
    }

    public void onGameFinished() {
        this.running = false;
    }
}
