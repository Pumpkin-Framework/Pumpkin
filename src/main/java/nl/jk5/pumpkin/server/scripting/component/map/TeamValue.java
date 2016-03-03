package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.api.mappack.Team;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.map.MapTeam;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TeamValue extends SimpleValue<Team> {

    public TeamValue(Team value) {
        super(value);
    }

    @Callback
    public Object[] setSubtitle(Context ctx, Arguments args){
        ((MapTeam) getValue()).setSubtitle(MapComponent.convertText(args, 0));
        return new Object[0];
    }

    @Callback
    public Object[] clearSubtitle(Context ctx, Arguments args){
        ((MapTeam) getValue()).clearSubtitle();
        return new Object[0];
    }

    @Callback
    public Object[] setSpawn(Context ctx, Arguments args){
        double x = args.checkDouble(0);
        double y = args.checkDouble(1);
        double z = args.checkDouble(2);
        double pitch = args.optDouble(3, 0);
        double yaw = args.optDouble(4, 0);
        getValue().setSpawn(x, y, z, (float) pitch, (float) yaw);
        return new Object[0];
    }

    @Callback
    public Object[] setFastRespawnTimer(Context ctx, Arguments args){
        int time = args.checkInteger(0);
        ((MapTeam) getValue()).setFastRespawnTimer(time);
        return new Object[0];
    }

    @Callback
    public Object[] getPlayers(Context ctx, Arguments args){
        Collection<Player> players = getValue().getMembers();
        List<PlayerValue> collect = players.stream().map(PlayerValue::new).collect(Collectors.toList());
        return new Object[]{collect};
    }

    @Callback
    public Object[] countdown(Context ctx, Arguments args){
        AtomicInteger secondsLeft = new AtomicInteger(args.checkInteger(0));
        Task[] task = new Task[1];
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder()
                .async()
                .interval(1, TimeUnit.SECONDS)
                .execute(() -> {
                    int seconds = secondsLeft.getAndDecrement();
                    if (seconds == 0) {
                        ((MapTeam) getValue()).getMap().getMachine().signal("countdown_done_" + getValue().getName());
                        task[0].cancel();
                        return;
                    }
                    Title.Builder builder = Title.builder().stay(0).fadeOut(30).fadeIn(0);
                    if (seconds == 3) {
                        builder.title(Text.of(TextColors.YELLOW, seconds));
                    } else if (seconds == 2) {
                        builder.title(Text.of(TextColors.GOLD, seconds));
                    } else if (seconds == 1) {
                        builder.title(Text.of(TextColors.RED, seconds));
                    } else {
                        builder.title(Text.of(TextColors.WHITE, seconds));
                    }
                    getValue().getMembers().forEach(p -> p.sendTitle(builder.build()));
                });
        task[0] = taskBuilder.submit(Pumpkin.instance());
        return new Object[0];
    }
}
