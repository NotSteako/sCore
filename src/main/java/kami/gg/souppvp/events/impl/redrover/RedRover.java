package kami.gg.souppvp.events.impl.redrover;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.player.RedRoverPlayer;
import kami.gg.souppvp.events.impl.redrover.player.RedRoverPlayerState;
import kami.gg.souppvp.events.impl.redrover.player.RedRoverTeam;
import kami.gg.souppvp.events.impl.redrover.task.RedRoverDraftTask;
import kami.gg.souppvp.events.impl.redrover.task.RedRoverRoundEndTask;
import kami.gg.souppvp.events.impl.redrover.task.RedRoverRoundStartTask;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.EventUtil;
import kami.gg.souppvp.util.PlayerSnapshot;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.TimeUtil;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public class RedRover {

    public static String EVENT_PREFIX = CC.translate("");

    private String name;
    @Setter private RedRoverState state = RedRoverState.WAITING;
    private RedRoverTask eventTask;
    private PlayerSnapshot host;
    private LinkedHashMap<UUID, RedRoverPlayer> eventPlayers = new LinkedHashMap<>();
    @Getter private List<UUID> spectators = new ArrayList<>();
    private int maxPlayers;
    @Getter @Setter private int totalPlayers;
    @Setter private Cooldown cooldown;

    @Setter private UUID redCaptain;
    @Setter private UUID blueCaptain;
    @Setter private RedRoverTeam pickingTeam = RedRoverTeam.RED;

    private RedRoverPlayer roundPlayerA;
    private RedRoverPlayer roundPlayerB;
    @Setter private long roundStart;

    public RedRover(Player player) {
        this.name = player.getName();
        this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
        this.maxPlayers = 100;
    }

    public void setEventTask(RedRoverTask task) {
        if (eventTask != null) eventTask.cancel();
        eventTask = task;
        if (eventTask != null) eventTask.runTaskTimer(SoupPvP.getInstance(), 0L, 20L);
    }

    public boolean isWaiting()  { return state == RedRoverState.WAITING; }
    public boolean isDrafting() { return state == RedRoverState.DRAFTING; }
    public boolean isFighting() { return state == RedRoverState.ROUND_FIGHTING; }

    public RedRoverPlayer getEventPlayer(Player player) {
        return eventPlayers.get(player.getUniqueId());
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (RedRoverPlayer rp : eventPlayers.values()) {
            Player p = rp.getPlayer();
            if (p != null) players.add(p);
        }
        return players;
    }

    public List<Player> getRemainingPlayers() {
        List<Player> players = new ArrayList<>();
        for (RedRoverPlayer rp : eventPlayers.values()) {
            if (rp.getState() == RedRoverPlayerState.WAITING) {
                Player p = rp.getPlayer();
                if (p != null) players.add(p);
            }
        }
        return players;
    }

    public List<RedRoverPlayer> getUndraftedPlayers() {
        List<RedRoverPlayer> list = new ArrayList<>();
        for (RedRoverPlayer rp : eventPlayers.values()) {
            if (rp.getState() == RedRoverPlayerState.WAITING && rp.getTeam() == RedRoverTeam.NONE && !rp.isCaptain()) {
                list.add(rp);
            }
        }
        return list;
    }

    public List<RedRoverPlayer> getTeam(RedRoverTeam team) {
        List<RedRoverPlayer> list = new ArrayList<>();
        for (RedRoverPlayer rp : eventPlayers.values()) {
            if (rp.getTeam() == team && rp.getState() == RedRoverPlayerState.WAITING) list.add(rp);
        }
        return list;
    }

    public void handleJoin(Player player) {
        eventPlayers.put(player.getUniqueId(), new RedRoverPlayer(player));
        broadcastMessage(CC.translate("&c" + player.getName() + " &7has joined the &cRed Rover &7Event! &f(" + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.IN_EVENT);
        EventUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn().add(0.5, 0, 0.5));
    }

    public void handleLeave(Player player) {
        if (isFighting(player.getUniqueId())) handleDeath(player);
        eventPlayers.remove(player.getUniqueId());
        if (state == RedRoverState.WAITING) {
            broadcastMessage(CC.translate("&c" + player.getName() + " &7has left the &cRed Rover &7Event! &f(" + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")"));
        }
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPAWN);
        PlayerUtil.resetPlayer(player);
    }

    public List<Player> getSpectatorsList() {
        return PlayerUtil.convertUUIDListToPlayerList(spectators);
    }

    public void handleDeath(Player player) {
        RedRoverPlayer loser = getEventPlayer(player);
        if (loser == null) return;
        loser.setState(RedRoverPlayerState.ELIMINATED);
        onDeath(player);
    }

    public void end() {
        SoupPvP.getInstance().getRedRoverHandler().setActiveRedRover(null);
        setEventTask(null);
        RedRoverTeam winningTeam = getWinningTeam();
        if (winningTeam == null || winningTeam == RedRoverTeam.NONE) {
            Bukkit.broadcastMessage(CC.translate("&cThe Red Rover Event has been cancelled."));
        } else {
            Bukkit.broadcastMessage(CC.translate("&7Team " + winningTeam.getColor() + winningTeam.getReadable() + " &7has won the &cRed Rover &7Event!"));
            for (RedRoverPlayer rp : eventPlayers.values()) {
                if (rp.getTeam() == winningTeam) {
                    Player p = rp.getPlayer();
                    if (p != null) {
                        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(p.getUniqueId());
                        profile.setEventsWon(profile.getEventsWon() + 1);
                        profile.setCredits(profile.getCredits() + 100);
                    }
                }
            }
        }
        for (RedRoverPlayer rp : eventPlayers.values()) {
            Player p = rp.getPlayer();
            if (p != null) {
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(p.getUniqueId());
                profile.setProfileState(ProfileState.SPAWN);
                PlayerUtil.resetPlayer(p);
            }
        }
        getSpectatorsList().forEach(this::removeSpectator);
    }

    public RedRoverTeam getWinningTeam() {
        int red = getTeam(RedRoverTeam.RED).size();
        int blue = getTeam(RedRoverTeam.BLUE).size();
        if (red > 0 && blue == 0) return RedRoverTeam.RED;
        if (blue > 0 && red == 0) return RedRoverTeam.BLUE;
        return null;
    }

    public boolean canEnd() { return getWinningTeam() != null; }

    public void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FancyMessage message = new FancyMessage(CC.translate("&c" + getHost().getUsername() + " &7is currently hosting a &cRed Rover &7Event! "));
            message.then("[Click Here]").color(ChatColor.GREEN).command("/redrover join").tooltip(ChatColor.GREEN + "Click to join!").then(" (" + getRemainingPlayers().size() + "/" + getMaxPlayers() + ")").color(ChatColor.WHITE);
            message.send(player);
        }
    }

    public void broadcastMessage(String message) {
        for (Player player : getPlayers())          player.sendMessage(EVENT_PREFIX + CC.translate(message));
        for (Player player : getSpectatorsList())   player.sendMessage(EVENT_PREFIX + CC.translate(message));
    }

    public void startDraft() {
        List<RedRoverPlayer> pool = new ArrayList<>(eventPlayers.values());
        pool.removeIf(rp -> rp.getState() != RedRoverPlayerState.WAITING);

        if (pool.size() < 2) {
            broadcastMessage("&cNot enough players to start a draft.");
            end();
            return;
        }

        Collections.shuffle(pool, new Random());
        RedRoverPlayer red = pool.get(0);
        RedRoverPlayer blue = pool.get(1);

        red.setCaptain(true);  red.setTeam(RedRoverTeam.RED);
        blue.setCaptain(true); blue.setTeam(RedRoverTeam.BLUE);

        this.redCaptain  = red.getUuid();
        this.blueCaptain = blue.getUuid();
        this.pickingTeam = RedRoverTeam.RED;
        setTotalPlayers(getPlayers().size());

        broadcastMessage("&7Captains have been chosen:");
        broadcastMessage("  &c[Red Captain] &f"  + red.getUsername());
        broadcastMessage("  &b[Blue Captain] &f" + blue.getUsername());

        if (getUndraftedPlayers().isEmpty()) {
            setState(RedRoverState.ROUND_STARTING);
            onRound();
            return;
        }

        setState(RedRoverState.DRAFTING);
        setEventTask(new RedRoverDraftTask(this));
        promptCaptain();
    }

    public void promptCaptain() {
        if (getUndraftedPlayers().isEmpty()) { finishDraft(); return; }
        UUID picker = pickingTeam == RedRoverTeam.RED ? redCaptain : blueCaptain;
        Player p = Bukkit.getPlayer(picker);
        if (p == null) { autoPickRandom(); return; }
        broadcastMessage("&7It is " + pickingTeam.getColor() + p.getName() + "&7's turn to pick a player.");
        new kami.gg.souppvp.events.impl.redrover.menu.CaptainPickMenu(this).openMenu(p);
    }

    public void pickPlayer(Player captain, UUID targetUuid) {
        RedRoverPlayer picker = getEventPlayer(captain);
        if (picker == null || !picker.isCaptain()) { captain.sendMessage(CC.translate("&cYou are not a captain.")); return; }
        if (picker.getTeam() != pickingTeam)       { captain.sendMessage(CC.translate("&cIt is not your turn to pick.")); return; }
        RedRoverPlayer target = eventPlayers.get(targetUuid);
        if (target == null || target.getTeam() != RedRoverTeam.NONE || target.isCaptain()) {
            captain.sendMessage(CC.translate("&cThat player cannot be picked.")); return;
        }
        target.setTeam(picker.getTeam());
        broadcastMessage(picker.getTeam().getColor() + captain.getName() + " &7picked " + picker.getTeam().getColor() + target.getUsername() + "&7.");
        if (eventTask instanceof RedRoverDraftTask) setEventTask(new RedRoverDraftTask(this));
        pickingTeam = (pickingTeam == RedRoverTeam.RED) ? RedRoverTeam.BLUE : RedRoverTeam.RED;
        if (getUndraftedPlayers().isEmpty()) finishDraft();
        else promptCaptain();
    }

    public void autoPickRandom() {
        List<RedRoverPlayer> pool = getUndraftedPlayers();
        if (pool.isEmpty()) { finishDraft(); return; }
        Collections.shuffle(pool, new Random());
        RedRoverPlayer target = pool.get(0);
        target.setTeam(pickingTeam);
        broadcastMessage("&7(Auto) " + pickingTeam.getColor() + target.getUsername() + " &7was assigned to team " + pickingTeam.getColor() + pickingTeam.getReadable() + "&7.");
        if (eventTask instanceof RedRoverDraftTask) setEventTask(new RedRoverDraftTask(this));
        pickingTeam = (pickingTeam == RedRoverTeam.RED) ? RedRoverTeam.BLUE : RedRoverTeam.RED;
        if (getUndraftedPlayers().isEmpty()) finishDraft();
        else promptCaptain();
    }

    public void finishDraft() {
        broadcastMessage("&7Draft complete! Match starting...");
        setState(RedRoverState.ROUND_STARTING);
        onRound();
    }

    public void onRound() {
        setState(RedRoverState.ROUND_STARTING);

        if (roundPlayerA == null || roundPlayerA.getState() == RedRoverPlayerState.ELIMINATED)
            roundPlayerA = nextFighter(RedRoverTeam.RED);
        if (roundPlayerB == null || roundPlayerB.getState() == RedRoverPlayerState.ELIMINATED)
            roundPlayerB = nextFighter(RedRoverTeam.BLUE);

        if (roundPlayerA == null || roundPlayerB == null) { end(); return; }

        Player playerA = roundPlayerA.getPlayer();
        Player playerB = roundPlayerB.getPlayer();
        if (playerA == null || playerB == null) { end(); return; }

        Location spawnA = SoupPvP.getInstance().getRedRoverHandler().getSpawnA();
        Location spawnB = SoupPvP.getInstance().getRedRoverHandler().getSpawnB();

        playerA.teleport(spawnA);
        playerB.teleport(spawnB);
        playerA.getInventory().clear();
        playerB.getInventory().clear();

        broadcastMessage("&7Next match: " + RedRoverTeam.RED.getColor() + roundPlayerA.getUsername() + " &7vs " + RedRoverTeam.BLUE.getColor() + roundPlayerB.getUsername());

        setEventTask(new RedRoverRoundStartTask(this));
    }

    private RedRoverPlayer nextFighter(RedRoverTeam team) {
        RedRoverPlayer chosen = null;
        for (RedRoverPlayer rp : eventPlayers.values()) {
            if (rp.getTeam() != team) continue;
            if (rp.getState() != RedRoverPlayerState.WAITING) continue;
            if (isFighting(rp.getUuid())) continue;
            if (chosen == null) { chosen = rp; continue; }
            if (rp.getRoundWins() < chosen.getRoundWins()) chosen = rp;
        }
        return chosen;
    }

    public void onDeath(Player player) {
        if (roundPlayerA == null || roundPlayerB == null) return;
        RedRoverPlayer winner = roundPlayerA.getUuid().equals(player.getUniqueId()) ? roundPlayerB : roundPlayerA;
        winner.incrementRoundWins();
        Player wp = winner.getPlayer();
        if (wp != null) wp.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
        broadcastMessage("&c" + winner.getUsername() + "&7 eliminated &c" + player.getName() + "&7! &f(Streak: " + winner.getRoundWins() + ")");

        if (roundPlayerA.getUuid().equals(player.getUniqueId())) roundPlayerA = null;
        else roundPlayerB = null;

        setState(RedRoverState.ROUND_ENDING);
        setEventTask(new RedRoverRoundEndTask(this));
    }

    public String getRoundDuration() {
        if (getState() == RedRoverState.ROUND_STARTING)  return "00:00";
        if (getState() == RedRoverState.ROUND_FIGHTING)  return TimeUtil.millisToTimer(System.currentTimeMillis() - roundStart);
        return "Ending";
    }

    public boolean isFighting(UUID uuid) {
        return (roundPlayerA != null && roundPlayerA.getUuid().equals(uuid))
                || (roundPlayerB != null && roundPlayerB.getUuid().equals(uuid));
    }

    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPECTATING_EVENT);
        EventUtil.resetPlayer(player);
        player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setProfileState(ProfileState.SPAWN);
        PlayerUtil.resetPlayer(player);
    }

}
