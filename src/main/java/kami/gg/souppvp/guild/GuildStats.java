package kami.gg.souppvp.guild;
/**
 * Lightweight value object holding aggregated stats summed across all members
 * of a guild. Populated by {@code GuildsHandler#aggregateStats}.
 */
public class GuildStats {

    public int memberCount;
    public int totalKills;
    public int totalDeaths;
    public int totalCredits;
    public int totalExperiences;
    public int totalEventsWon;
    public int highestKillstreak; // best of any single member

    public double averageKDR() {
        if (totalDeaths == 0) return totalKills;
        return Math.round((totalKills * 100.0 / totalDeaths)) / 100.0;
    }
}