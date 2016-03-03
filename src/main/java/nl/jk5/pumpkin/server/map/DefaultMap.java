package nl.jk5.pumpkin.server.map;

import com.google.common.base.Objects;
import nl.jk5.pumpkin.api.mappack.*;
import nl.jk5.pumpkin.api.mappack.game.Game;
import nl.jk5.pumpkin.api.mappack.game.Winnable;
import nl.jk5.pumpkin.api.mappack.game.WinnablePlayerWrapper;
import nl.jk5.pumpkin.api.mappack.game.stat.StatManager;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.map.game.MapGame;
import nl.jk5.pumpkin.server.map.stat.MapStatManager;
import nl.jk5.pumpkin.server.scripting.*;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.FileSystem;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.FileSystemComponent;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.FileSystems;
import nl.jk5.pumpkin.server.scripting.component.map.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultMap implements nl.jk5.pumpkin.api.mappack.Map, AbstractValue {

    private final Mappack mappack;
    private final Pumpkin pumpkin;
    private final File saveDir;

    private final List<DefaultMapWorld> worlds = new ArrayList<>();
    private final List<MapTeam> teams = new ArrayList<>();

    private final Map<UUID, Team> userTeams = new HashMap<>();
    private final Map<Player, Team> playerTeams = new HashMap<>();

    private final Set<Player> players = new HashSet<>();

    private final Machine machine;
    private final MapGame game;
    private final MapStatManager statManager;

    @SuppressWarnings("NullableProblems")
    private MapWorld defaultWorld;

    private boolean firstTick = true;

    public DefaultMap(Mappack mappack, Pumpkin pumpkin, File saveDir){
        this.mappack = mappack;
        this.pumpkin = pumpkin;
        this.saveDir = saveDir;

        this.game = new MapGame(this);

        this.mappack.getTeams().forEach(t -> this.teams.add(new MapTeam(t, this)));

        this.statManager = new MapStatManager(this);

        this.machine = new DefaultMachine(this);

        FileSystem fs = FileSystems.fromClass(Pumpkin.class, "pumpkin", "lua/pumpkinos");
        FileSystemComponent rootfs = new FileSystemComponent("/dev/pknrootfs", fs);

        fs = FileSystems.fromDirectory(new File("nailtest"));
        FileSystemComponent gamefs = new FileSystemComponent("/dev/gamefs", fs);

        this.machine.addComponent(rootfs);
        this.machine.addComponent(gamefs);
        this.machine.addComponent(new MapComponent(this));
    }

    @Override
    public Mappack getMappack() {
        return mappack;
    }

    public void addWorld(DefaultMapWorld world) {
        this.worlds.add(world);
        if(world.getConfig().isDefault()){
            this.defaultWorld = world;
        }
        this.statManager.onWorldAdded(world);
    }

    @Override
    public void tick() {
        if(this.firstTick){
            this.machine.start();
            this.firstTick = false;
        }
        ((DefaultMachine) this.machine).update();
    }

    public Machine getMachine() {
        return this.machine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<MapWorld> getWorlds() {
        return ((Collection) worlds);
    }

    @Override
    public MapWorld getDefaultWorld() {
        return this.defaultWorld;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Team> getTeams() {
        return ((Collection) this.teams);
    }

    @Override
    public void addPlayerToTeam(Player player, Team team){
        if(this.userTeams.containsKey(player.getUniqueId())){
            this.removePlayerFromTeam(player, false);
        }
        this.userTeams.put(player.getUniqueId(), team);
        this.playerTeams.put(player, team);
        this.send(Text.of(TextColors.GREEN, "Player " + player.getName() + " is added to team " + team.getName()));

        this.players.forEach(this::initScoreboard);
    }

    @Override
    public void removePlayerFromTeam(Player player){
        this.removePlayerFromTeam(player, true);
    }

    private void removePlayerFromTeam(Player player, boolean log){
        Team team = this.userTeams.remove(player.getUniqueId());
        this.playerTeams.remove(player);
        if(team != null && log){
            this.send(Text.of(TextColors.GREEN, "Player " + player.getName() + " is removed from team " + team.getName()));
        }

        this.players.forEach(this::initScoreboard);
    }

    @Override
    public Optional<Team> getPlayerTeam(Player player){
        return Optional.ofNullable(this.userTeams.get(player.getUniqueId()));
    }

    @Override
    public Optional<Team> teamByName(String name){
        return this.teams.stream().map(t -> (Team) t).filter(t -> t.getName().equals(name)).findFirst();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mappack", mappack)
                .add("worlds", worlds)
                .toString();
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        return this.getWorlds()
                .stream()
                .map(w -> w.getWorld().getEntities(e -> e instanceof Player))
                .flatMap(Collection::stream)
                .map(e -> (MessageReceiver) e)
                .collect(Collectors.toList());
    }

    @Override
    public Map<UUID, Team> getUserTeams() {
        return userTeams;
    }

    @Override
    public Map<Player, Team> getPlayerTeams() {
        return playerTeams;
    }

    @Override
    public File getSaveDirectory() {
        return this.saveDir;
    }

    public void onPlayerJoin(Player player) {
        Log.info("Player " + player.getName() + " joined map " + this.toString());
        this.players.add(player);

        if(this.userTeams.containsKey(player.getUniqueId())){
            Team team = this.userTeams.get(player.getUniqueId());
            this.playerTeams.put(player, team); //TODO: scoreboard update
        }

        this.initScoreboard(player);

        this.game.onPlayerJoin(player);

        this.pumpkin.getMapEventListener().onPlayerJoinMap(player, this);

        this.machine.signal("player_join", player.getName());
    }

    public void onPlayerLeft(Player player) {
        Log.info("Player " + player.getName() + " left map " + this.toString());
        this.players.remove(player);

        if(this.playerTeams.containsKey(player)){
            this.playerTeams.remove(player); //TODO: scoreboard update
        }

        this.game.onPlayerLeft(player);

        this.machine.signal("player_leave", player.getName());
    }

    @Override
    public Collection<Player> getPlayers() {
        return this.players;
    }

    private void initScoreboard(Player player){
        List<org.spongepowered.api.scoreboard.Team> teams = this.teams.stream().map(t -> {
            return org.spongepowered.api.scoreboard.Team.builder()
                    .allowFriendlyFire(true) // Friendly fire is handled in MapEventListener#onAttack
                    .canSeeFriendlyInvisibles(true) //TODO
                    .color(t.getColor())
                    .prefix(Text.of(t.getColor(), ""))
                    .name(t.getName())
                    .displayName(Text.of(t.getColor(), t.getName()))
                    .members(t.getMembers().stream().map(TeamMember::getTeamRepresentation).collect(Collectors.toSet()))
                    .build(); //TODO: prefix, suffix, displayname
        }).collect(Collectors.toList());
        Scoreboard scoreboard = Scoreboard.builder().teams(teams).build();
        player.setScoreboard(scoreboard);
    }

    @Override
    public Optional<Game> getGame() {
        return Optional.of(this.game);
    }

    public boolean isInActiveGame(Player player){
        return this.game.isInActiveGame(player);
    }

    public void onSignal(DefaultMachine.SimpleSignal signal) {
        if(signal.getName().equals("game_finished")){
            this.game.onGameFinished();
        }
    }

    @Override
    public StatManager getStatManager() {
        return this.statManager;
    }

    @Callback
    public Object[] sendMessage(Context ctx, Arguments args){
        this.send(MapComponent.getText(args, 0));
        return new Object[0];
    }

    @Callback
    public Object[] getStat(Context ctx, Arguments args){
        return new Object[]{new StatValue(this.statManager.byName(args.checkString(0)))};
    }

    @Callback
    public Object[] getTeam(Context ctx, Arguments args){
        Optional<Team> team = this.teamByName(args.checkString(0));
        if(team.isPresent()){
            return new Object[]{new TeamValue(team.get())};
        }else{
            return new Object[0];
        }
    }

    @Callback
    public Object[] getWorld(Context ctx, Arguments args){
        Optional<MapWorld> world = this.getWorlds().stream().filter(w -> w.getConfig().getName().equals(args.checkString(0))).findAny();
        if(world.isPresent()){
            return new Object[]{new WorldValue(world.get())};
        }else{
            return new Object[0];
        }
    }

    @Callback
    public Object[] setWinTimeout(Context ctx, Arguments args){
        int timeout = args.checkInteger(0);
        Object e = args.checkAny(1);
        if(!(e instanceof SimpleValue) || !(((SimpleValue) e).getValue() instanceof Team)){
            throw new IllegalArgumentException("#2: expected team object");
        }
        game.setWinTimeout(timeout, ((Team) ((SimpleValue) e).getValue()));
        return new Object[]{};
    }

    @Callback
    public Object[] setWinner(Context ctx, Arguments args){
        Object e = args.checkAny(0);
        if(!(e instanceof SimpleValue) || (!(((SimpleValue) e).getValue() instanceof Team) && !(((SimpleValue) e).getValue() instanceof Player))){
            throw new IllegalArgumentException("#1: expected player/team object");
        }
        Winnable winner;
        if(((SimpleValue) e).getValue() instanceof Player){
            winner = new WinnablePlayerWrapper((Player) ((SimpleValue) e).getValue());
        }else if(((SimpleValue) e).getValue() instanceof Team){
            winner = ((Team) ((SimpleValue) e).getValue());
        }else throw new IllegalArgumentException("#1: expected player/team object");
        game.setWinner(winner);
        return new Object[]{};
    }

    public void onSubtitleTick() {
        this.teams.forEach(MapTeam::onSubtitleTick);
    }
}
