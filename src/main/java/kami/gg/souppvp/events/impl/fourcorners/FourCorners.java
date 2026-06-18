package kami.gg.souppvp.events.impl.fourcorners;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.player.FourCornersPlayer;
import kami.gg.souppvp.events.impl.fourcorners.player.FourCornersPlayerState;
import kami.gg.souppvp.events.impl.fourcorners.task.FourCornersRoundEndTask;
import kami.gg.souppvp.events.impl.fourcorners.task.FourCornersRoundStartTask;
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
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class FourCorners {

    public static String EVENT_PREFIX = CC.translate("");

    private String name;
    @Setter private FourCornersState state = FourCornersState.WAITING;
    private FourCornersTask eventTask;
    private PlayerSnapshot host;
    private LinkedHashMap<UUID, FourCornersPlayer> eventPlayers = new LinkedHashMap<>();
    @Getter private List<UUID> spectators = new ArrayList<>();
    private int maxPlayers;
    @Getter @Setter private int totalPlayers;
    @Setter private Cooldown cooldown;
    @Setter private long roundStart;
    @Setter private int roundNumber = 0;
    @Setter private FourCornersCorner dropCorner;

    public FourCorners(Player player) {
        this.name = player.getName();
        this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
        this.maxPlayers = 40;
    }

    public void setEventTask(FourCornersTask task) {
        if (eventTask != null) eventTask.cancel();
        eventTask = task;
        if (eventTask != null) eventTask.runTaskTimer(SoupPvP.getInstance(), 0L, 20L);
    }

    public boolean isWaiting() { return state == FourCornersState.WAITING; }
    public boolean isFighting() { return state == FourCornersState.ROUND_FIGHTING; }

    public FourCornersPlayer getEventPlayer(Player player) { return eventPlayers.get(player.getUniqueId()); }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (FourCornersPlayer p : eventPlayers.values()) {
            Player player = p.getPlayer();
            if (player != null) players.add(player);
        }
        return players;
    }

    public List<Player> getRemainingPlayers() {
        List<Player> players = new ArrayList<>();
        for (FourCornersPlayer p : eventPlayers.values()) {
            if (p.getState() == FourCornersPlayerState.WAITING) {
                Player player = p.getPlayer();
                if (player != null) players.add(player);
            }
        }
        return players;
    }

    public void handleJoin(Player player) {
        eventPlayers.put(player.getUniqueId(), new FourCornersPlayer(player));
        broadcastMessage(CC.translate("&b" + player.getName() + " &7has joined the &b4Corners &7Event! &f("
                + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setFourCornersEvent(this);
        profile.setProfileState(ProfileState.IN_EVENT);
        EventUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn().add(0.5, 0, 0.5));
    }

    public void handleLeave(Player player) {
        if (isAlive(player.getUniqueId()) && isFighting()) handleDeath(player);
        eventPlayers.remove(player.getUniqueId());
        if (state == FourCornersState.WAITING) {
            broadcastMessage(CC.translate("&b" + player.getName() + " &7has left the &b4Corners &7Event! &f("
                    + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        }
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPAWN);
        profile.setFourCornersEvent(null);
        PlayerUtil.resetPlayer(player);
    }

    protected List<Player> getSpectatorsList() { return PlayerUtil.convertUUIDListToPlayerList(spectators); }

    public void handleDeath(Player player) {
        FourCornersPlayer eliminated = getEventPlayer(player);
        if (eliminated == null) return;
        eliminated.setState(FourCornersPlayerState.ELIMINATED);
        broadcastMessage(CC.translate("&b" + player.getName() + " &7was eliminated!"));
        player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn());
        PlayerUtil.resetPlayer(player);
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPECTATING_EVENT);
    }

    public void end() {
        SoupPvP.getInstance().getFourCornersHandler().setActiveEvent(null);
        setEventTask(null);
        Player winner = this.getWinner();
        if (winner == null) {
            Bukkit.broadcastMessage(CC.translate("&cThe 4Corners Event has been cancelled."));
        } else {
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(winner.getUniqueId());
            profile.setEventsWon(profile.getEventsWon() + 1);
            profile.setCredits(profile.getCredits() + 100);
            Bukkit.broadcastMessage(CC.translate("&b" + winner.getName() + " &7has won the &b4Corners &7Event!"));
        }
        for (FourCornersPlayer p : eventPlayers.values()) {
            Player player = p.getPlayer();
            if (player != null) {
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                profile.setProfileState(ProfileState.SPAWN);
                profile.setFourCornersEvent(null);
                PlayerUtil.resetPlayer(player);
            }
        }
        getSpectatorsList().forEach(this::removeSpectator);
    }

    public boolean canEnd() {
        int remaining = 0;
        for (FourCornersPlayer p : eventPlayers.values()) {
            if (p.getState() == FourCornersPlayerState.WAITING) remaining++;
        }
        return remaining <= 1;
    }

    public Player getWinner() {
        for (FourCornersPlayer p : eventPlayers.values()) {
            if (p.getState() != FourCornersPlayerState.ELIMINATED) return p.getPlayer();
        }
        return null;
    }

    public void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FancyMessage message = new FancyMessage(CC.translate("&b" + getHost().getUsername()
                    + " &7is currently hosting a &b4Corners &7Event! "));
            message.then("[Click Here]").color(ChatColor.GREEN).command("/fourcorners join")
                    .tooltip(ChatColor.GREEN + "Click to join!")
                    .then(" (" + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")").color(ChatColor.WHITE);
            message.send(player);
        }
    }

    public void broadcastMessage(String message) {
        for (Player player : getPlayers()) player.sendMessage(EVENT_PREFIX + CC.translate(message));
        for (Player player : getSpectatorsList()) player.sendMessage(EVENT_PREFIX + CC.translate(message));
    }

    public void onRound() {
        setState(FourCornersState.ROUND_STARTING);
        roundNumber++;
        dropCorner = null;

        FourCornersCorner[] corners = FourCornersCorner.values();
        int i = 0;
        for (FourCornersPlayer p : eventPlayers.values()) {
            if (p.getState() != FourCornersPlayerState.WAITING) continue;
            Player player = p.getPlayer();
            if (player == null) continue;
            FourCornersCorner corner = corners[i % corners.length];
            Location loc = SoupPvP.getInstance().getFourCornersHandler().getCornerLocation(corner);
            if (loc != null) player.teleport(loc.clone().add(0.5, 0, 0.5));
            EventUtil.resetPlayer(player);
            player.getInventory().clear();
            i++;
        }

        broadcastMessage("&7Round &b" + roundNumber + " &7- pick a corner!");
        setEventTask(new FourCornersRoundStartTask(this));
    }

    public void onBridgesDrop() {
        // Pick a random DROP corner (the one that falls). The other 3 stay safe.
        FourCornersCorner[] corners = FourCornersCorner.values();
        dropCorner = corners[ThreadLocalRandom.current().nextInt(corners.length)];
        broadcastMessage("&7The &" + dropCorner.getColor().getChar() + dropCorner.getDisplayName()
                + " &7corner has dropped!");

        double radius = SoupPvP.getInstance().getFourCornersHandler().getCornerRadius();
        double radiusSq = radius * radius;
        Location dropLoc = SoupPvP.getInstance().getFourCornersHandler().getCornerLocation(dropCorner);

        // Visual: lightning at the corner that just dropped
        if (dropLoc != null && dropLoc.getWorld() != null) {
            dropLoc.getWorld().strikeLightningEffect(dropLoc.clone().add(0.5, 0, 0.5));
        }

        List<Player> toEliminate = new ArrayList<>();
        for (FourCornersPlayer p : eventPlayers.values()) {
            if (p.getState() != FourCornersPlayerState.WAITING) continue;
            Player player = p.getPlayer();
            if (player == null) continue;
            if (dropLoc == null || !player.getWorld().equals(dropLoc.getWorld())) continue;
            double dx = player.getLocation().getX() - (dropLoc.getX() + 0.5);
            double dz = player.getLocation().getZ() - (dropLoc.getZ() + 0.5);
            // Anyone INSIDE the drop corner's radius dies; the other 3 corners stay safe.
            if ((dx * dx + dz * dz) <= radiusSq) {
                toEliminate.add(player);
            }
        }

        for (Player player : toEliminate) handleDeath(player);

        setState(FourCornersState.ROUND_ENDING);
        setEventTask(new FourCornersRoundEndTask(this));
    }

    public String getRoundDuration() {
        if (getState() == FourCornersState.ROUND_STARTING) return "00:00";
        if (getState() == FourCornersState.ROUND_FIGHTING)
            return kami.gg.souppvp.util.TimeUtil.millisToTimer(System.currentTimeMillis() - roundStart);
        return "Ending";
    }

    public boolean isAlive(UUID uuid) {
        FourCornersPlayer p = eventPlayers.get(uuid);
        return p != null && p.getState() == FourCornersPlayerState.WAITING;
    }

    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setFourCornersEvent(this);
        profile.setProfileState(ProfileState.SPECTATING_EVENT);
        EventUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn());
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setFourCornersEvent(null);
        profile.setProfileState(ProfileState.SPAWN);
        PlayerUtil.resetPlayer(player);
    }
}