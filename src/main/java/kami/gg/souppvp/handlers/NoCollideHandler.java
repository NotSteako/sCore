package kami.gg.souppvp.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Disables player↔player pushing globally.
 *
 * Assemble gives each player their own scoreboard, so a no-collide
 * team has to live on every player's scoreboard with every online
 * player as an entry.
 */
public class NoCollideHandler {

    public static final String TEAM_NAME = "spvp_nc";

    /**
     * Ensure {@code player}'s personal scoreboard contains the
     * no-collide team and that every online player is listed in it.
     */
    public static void apply(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;

        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!team.hasEntry(online.getName())) {
                team.addEntry(online.getName());
            }
        }
    }

    /**
     * Refresh every online player's scoreboard. Call after a join,
     * a scoreboard swap, or any time the roster changes.
     */
    public static void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            apply(viewer);
        }
    }

    /** Remove a player's entry from every other player's team. */
    public static void removeEverywhere(Player leaving) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(leaving)) continue;
            Scoreboard board = viewer.getScoreboard();
            if (board == null) continue;
            Team team = board.getTeam(TEAM_NAME);
            if (team != null && team.hasEntry(leaving.getName())) {
                team.removeEntry(leaving.getName());
            }
        }
    }
}