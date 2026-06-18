package kami.gg.souppvp.events.impl.colourshuffle;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.colourshuffle.player.ColourShufflePlayer;
import kami.gg.souppvp.events.impl.colourshuffle.player.ColourShufflePlayerState;
import kami.gg.souppvp.events.impl.colourshuffle.task.ColourShuffleRoundTask;
import kami.gg.souppvp.events.impl.colourshuffle.task.ColourShuffleStartTask;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.EventUtil;
import kami.gg.souppvp.util.PlayerSnapshot;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class ColourShuffle {

    public static final String EVENT_PREFIX = CC.translate("");

    /** All 16 wool colours used to paint the floor. */
    public static final Material[] WOOL_PALETTE = new Material[]{
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL
    };

    private final String name;
    @Setter private ColourShuffleState state = ColourShuffleState.WAITING;
    private ColourShuffleTask eventTask;
    private final PlayerSnapshot host;
    private final LinkedHashMap<UUID, ColourShufflePlayer> eventPlayers = new LinkedHashMap<>();
    private final List<UUID> spectators = new ArrayList<>();
    private final int maxPlayers;
    @Setter private int totalPlayers;
    @Setter private Cooldown cooldown;
    @Setter private int currentRound;
    @Setter private Material safeColour;
    @Setter private long roundStart;

    public ColourShuffle(Player player) {
        this.name = player.getName();
        this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
        this.maxPlayers = 100;
    }

    public void setEventTask(ColourShuffleTask task) {
        if (eventTask != null) eventTask.cancel();
        eventTask = task;
        if (eventTask != null) eventTask.runTaskTimer(SoupPvP.getInstance(), 0L, 20L);
    }

    public boolean isWaiting() { return state == ColourShuffleState.WAITING; }
    public boolean isRunning() { return state == ColourShuffleState.ROUND_RUNNING; }

    public ColourShufflePlayer getEventPlayer(Player player) { return eventPlayers.get(player.getUniqueId()); }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (ColourShufflePlayer p : eventPlayers.values()) {
            Player pl = p.getPlayer();
            if (pl != null) players.add(pl);
        }
        return players;
    }

    public List<Player> getRemainingPlayers() {
        List<Player> players = new ArrayList<>();
        for (ColourShufflePlayer p : eventPlayers.values()) {
            if (p.getState() == ColourShufflePlayerState.WAITING) {
                Player pl = p.getPlayer();
                if (pl != null) players.add(pl);
            }
        }
        return players;
    }

    public void handleJoin(Player player) {
        eventPlayers.put(player.getUniqueId(), new ColourShufflePlayer(player));
        broadcastMessage(CC.translate("&d" + player.getName() + " &7has joined the &dColour Shuffle &7Event! &f("
                + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setColourShuffleEvent(this);
        profile.setProfileState(ProfileState.IN_EVENT);
        EventUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getColourShuffleHandler().getSpectatorSpawn().clone().add(0.5, 0, 0.5));
    }

    public void handleLeave(Player player) {
        eventPlayers.remove(player.getUniqueId());
        if (state == ColourShuffleState.WAITING) {
            broadcastMessage(CC.translate("&d" + player.getName() + " &7has left the &dColour Shuffle &7Event! &f("
                    + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        }
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPAWN);
        profile.setColourShuffleEvent(null);
        PlayerUtil.resetPlayer(player);
    }

    /** Called when a player falls into the void or otherwise dies during a round. */
    public void handleElimination(Player player) {
        ColourShufflePlayer csp = getEventPlayer(player);
        if (csp == null || csp.getState() == ColourShufflePlayerState.ELIMINATED) return;
        csp.setState(ColourShufflePlayerState.ELIMINATED);
        broadcastMessage(CC.translate("&7&l[&d&lCS&7&l] &f" + player.getName() + " &7was &celiminated &7on round &d" + currentRound + "&7!"));
        spectators.add(player.getUniqueId());
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPECTATING_EVENT);
        PlayerUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getColourShuffleHandler().getSpectatorSpawn().clone().add(0.5, 0, 0.5));
        // If only one player remaining, end the event.
        if (canEnd()) {
            end();
        }
    }

    public boolean canEnd() {
        int remaining = 0;
        for (ColourShufflePlayer csp : eventPlayers.values()) {
            if (csp.getState() == ColourShufflePlayerState.WAITING) remaining++;
        }
        return remaining <= 1;
    }

    public Player getWinner() {
        for (ColourShufflePlayer csp : eventPlayers.values()) {
            if (csp.getState() != ColourShufflePlayerState.ELIMINATED) return csp.getPlayer();
        }
        return null;
    }

    public void end() {
        SoupPvP.getInstance().getColourShuffleHandler().clearFloor();
        SoupPvP.getInstance().getColourShuffleHandler().setActiveEvent(null);
        setEventTask(null);
        Player winner = getWinner();
        if (winner == null) {
            Bukkit.broadcastMessage(CC.translate("&cThe Colour Shuffle Event has been cancelled."));
        } else {
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(winner.getUniqueId());
            profile.setEventsWon(profile.getEventsWon() + 1);
            profile.setCredits(profile.getCredits() + 100);
            Bukkit.broadcastMessage(CC.translate("&d" + winner.getName() + " &7has won the &dColour Shuffle &7Event!"));
        }
        for (ColourShufflePlayer csp : eventPlayers.values()) {
            Player pl = csp.getPlayer();
            if (pl != null) {
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(pl.getUniqueId());
                profile.setProfileState(ProfileState.SPAWN);
                profile.setColourShuffleEvent(null);
                PlayerUtil.resetPlayer(pl);
            }
        }
    }

    public void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FancyMessage message = new FancyMessage(CC.translate("&d" + host.getUsername() + " &7is currently hosting a &dColour Shuffle &7Event! "));
            message.then("[Click Here]").color(ChatColor.GREEN).command("/colourshuffle join").tooltip(ChatColor.GREEN + "Click to join!")
                    .then(" (" + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")").color(ChatColor.WHITE);
            message.send(player);
        }
    }

    public void broadcastMessage(String message) {
        for (Player player : getPlayers()) player.sendMessage(EVENT_PREFIX + CC.translate(message));
        for (UUID uuid : spectators) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(EVENT_PREFIX + CC.translate(message));
        }
    }

    /** Begin the next round: shuffle the floor, pick a safe colour, teleport everyone above it. */
    public void onRound() {
        currentRound++;
        setState(ColourShuffleState.ROUND_STARTING);

        // Shuffle floor randomly and pick a safe colour.
        SoupPvP.getInstance().getColourShuffleHandler().shuffleFloor();
        safeColour = WOOL_PALETTE[ThreadLocalRandom.current().nextInt(WOOL_PALETTE.length)];

        // Teleport everyone (alive only) above the floor.
        for (Player player : getRemainingPlayers()) {
            player.teleport(SoupPvP.getInstance().getColourShuffleHandler().getFloorTeleportLocation());
        }
        broadcastMessage(CC.translate("&7Round &d" + currentRound + "&7! Safe colour: &r" + colourName(safeColour) + "&7. Get on it!"));
        setEventTask(new ColourShuffleRoundTask(this));
    }

    public String getCurrentColourName() {
        return safeColour == null ? "&7None" : colourName(safeColour);
    }

    public static String colourName(Material wool) {
        String n = wool.name().replace("_WOOL", "");
        ChatColor cc = ChatColor.WHITE;
        switch (wool) {
            case ORANGE_WOOL:    cc = ChatColor.GOLD; break;
            case MAGENTA_WOOL:   cc = ChatColor.LIGHT_PURPLE; break;
            case LIGHT_BLUE_WOOL:cc = ChatColor.AQUA; break;
            case YELLOW_WOOL:    cc = ChatColor.YELLOW; break;
            case LIME_WOOL:      cc = ChatColor.GREEN; break;
            case PINK_WOOL:      cc = ChatColor.LIGHT_PURPLE; break;
            case GRAY_WOOL:      cc = ChatColor.DARK_GRAY; break;
            case LIGHT_GRAY_WOOL:cc = ChatColor.GRAY; break;
            case CYAN_WOOL:      cc = ChatColor.DARK_AQUA; break;
            case PURPLE_WOOL:    cc = ChatColor.DARK_PURPLE; break;
            case BLUE_WOOL:      cc = ChatColor.BLUE; break;
            case BROWN_WOOL:     cc = ChatColor.DARK_RED; break;
            case GREEN_WOOL:     cc = ChatColor.DARK_GREEN; break;
            case RED_WOOL:       cc = ChatColor.RED; break;
            case BLACK_WOOL:     cc = ChatColor.DARK_GRAY; break;
            case WHITE_WOOL:     cc = ChatColor.WHITE; break;
            default: break;
        }
        return cc + n;
    }

    public void unused_placeholder_call() {
        // Static reference to ensure ColourShuffleStartTask is resolvable from this file.
        new ColourShuffleStartTask(this);
    }
}
