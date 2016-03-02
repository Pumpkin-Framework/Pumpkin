package nl.jk5.pumpkin.server.map.game;

import com.google.common.collect.ImmutableList;
import nl.jk5.pumpkin.api.mappack.game.Game;
import nl.jk5.pumpkin.api.mappack.game.GameStartResult;
import nl.jk5.pumpkin.api.mappack.game.Winnable;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.map.DefaultMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MapGame implements Game {

    private final DefaultMap map;

    private boolean starting = false;
    private boolean running = false;

    private Task startTask;
    private Winnable winner;

    private List<UUID> participantIds = Collections.emptyList();
    private List<Player> participants = Collections.emptyList();

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
        this.participantIds = ImmutableList.copyOf(map.getPlayers().stream().map(Player::getUniqueId).collect(Collectors.toList()));
        this.participants = ImmutableList.copyOf(map.getPlayers());

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
                    this.participants.forEach(p -> p.sendTitle(builder.build()));
                }).submit(Pumpkin.instance());

        return GameStartResult.success();
    }

    private void doStart(){
        this.startTask.cancel();
        this.starting = false;
        this.running = true;
        this.winner = null;

        this.participants.forEach(p -> {
            p.offer(Keys.HEALTH, p.maxHealth().get());
            p.offer(Keys.FOOD_LEVEL, p.getFoodData().foodLevel().getMaxValue());
            p.getFoodData().exhaustion().set(p.getFoodData().exhaustion().getMaxValue());
            p.getFoodData().saturation().set(p.getFoodData().saturation().getMaxValue());
        });

        Title startTitle = Title.builder().stay(40).fadeIn(0).fadeOut(20).title(Text.of(TextColors.GREEN, "GO!")).build();
        this.participants.forEach(p -> p.sendTitle(startTitle));

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
        return this.participants.contains(player);
    }

    public void onGameFinished() {
        this.running = false;

        if(this.winner == null){
            // No winner known. Just send a 'Game over' message
            Title title = Title.builder().stay(80).fadeIn(0).fadeOut(20).title(Text.of(TextColors.GOLD, "Game Over!")).build();
            this.participants.forEach(p -> p.sendTitle(title));
        }else{
            // TODO: 2-3-16 Seperate message for spectators
            Title winTitle = Title.builder().stay(80).fadeIn(0).fadeOut(20).title(Text.of(TextColors.GREEN, "You Win!")).build();
            Title loseTitle = Title.builder().stay(80).fadeIn(0).fadeOut(20).title(Text.of(TextColors.RED, "You Lost!")).subtitle(Text.of(this.winner.getWinnableDescription(), TextColors.GOLD, " won the game")).build();
            this.winner.getWinners().forEach(p -> p.sendTitle(winTitle));
            this.participants.stream().filter(e -> !this.winner.getWinners().contains(e)).forEach(p -> p.sendTitle(loseTitle));
        }
    }

    public void onGameCrashed(String reason) {
        if(!this.running){
            return;
        }
        this.running = false;

        this.map.send(Text.of(TextColors.RED, "The game script crashed. Game stopped"));
        Log.error("Game script crashed: " + reason);
    }

    @Override
    public void onPlayerJoin(Player player) {
        if(this.starting || this.running){
            if(this.participantIds.contains(player.getUniqueId())){
                this.participants = ImmutableList.<Player>builder().addAll(this.participants).add(player).build();
            }
        }else{
            if(!this.participantIds.contains(player.getUniqueId())){
                this.participantIds = ImmutableList.<UUID>builder().addAll(this.participantIds).add(player.getUniqueId()).build();
            }
            this.participants = ImmutableList.<Player>builder().addAll(this.participants).add(player).build();
        }
    }

    @Override
    public void onPlayerLeft(Player player) {
        if(this.starting || this.running){
            this.participants = ImmutableList.copyOf(this.participants.stream().filter(p -> p != player).collect(Collectors.toList()));
        }else{
            this.participantIds = ImmutableList.copyOf(this.participantIds.stream().filter(p -> p != player.getUniqueId()).collect(Collectors.toList()));
            this.participants = ImmutableList.copyOf(this.participants.stream().filter(p -> p != player).collect(Collectors.toList()));
        }
    }
}
