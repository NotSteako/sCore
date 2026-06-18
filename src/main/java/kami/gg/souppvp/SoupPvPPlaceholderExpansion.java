package kami.gg.souppvp;

import kami.gg.souppvp.handlers.LeaderboardHandler;
import kami.gg.souppvp.profile.Profile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SoupPvPPlaceholderExpansion extends PlaceholderExpansion {

    private final SoupPvP plugin;

    public SoupPvPPlaceholderExpansion(SoupPvP plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "souppvp"; }
    @Override public @NotNull String getAuthor()     { return String.join(", ", plugin.getDescription().getAuthors()); }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // %souppvp_leaderboard_<category>_<i>_(name|value)%
        if (params.startsWith("leaderboard_")) {
            return resolveLeaderboard(params.substring("leaderboard_".length()));
        }

        // per-player stats need a player
        if (player == null) return "";
        Profile profile = plugin.getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile == null && player.getName() != null) {
            profile = plugin.getProfilesHandler().getProfileByName(player.getName());
        }
        if (profile == null) return "";

        switch (params.toLowerCase()) {
            case "kills":              return String.valueOf(profile.getKills());
            case "deaths":             return String.valueOf(profile.getDeaths());
            case "credits":            return String.valueOf(profile.getCredits());
            case "bounty":             return String.valueOf(profile.getBounty());
            case "experiences":        return String.valueOf(profile.getExperiences());
            case "tier":               return String.valueOf(profile.getTier().getTierLevel());
            case "currentkillstreak":  return String.valueOf(profile.getCurrentKillstreak());
            case "highestkillstreak":  return String.valueOf(profile.getHighestKillstreak());
            case "eventswon":          return String.valueOf(profile.getEventsWon());
            case "wagerswon":          return String.valueOf(profile.getWagersWon());
            case "wagerslost":         return String.valueOf(profile.getWagersLost());
            case "totalwagergames":    return String.valueOf(profile.getTotalWagerGames());
            case "winpercent":         return String.valueOf(profile.getWinPercent());
            case "kit":                return profile.getCurrentKit() == null ? "" : profile.getCurrentKit().getName();
            case "kdr":                return formatKdr(profile.getKills(), profile.getDeaths());
            default:                   return null;
        }
    }

    /** params here is "<category>_<i>_<name|value>"  (i is 1-based) */
    private String resolveLeaderboard(String params) {

        int lastUnderscore = params.lastIndexOf('_');
        if (lastUnderscore <= 0) {
            return "";
        }

        String suffix = params.substring(lastUnderscore + 1).toLowerCase(); // name/value
        String head = params.substring(0, lastUnderscore);

        int secondLast = head.lastIndexOf('_');
        if (secondLast <= 0) {
            return "";
        }

        String category = head.substring(0, secondLast).toLowerCase();
        String indexToken = head.substring(secondLast + 1);

        int index;
        try {
            index = Integer.parseInt(indexToken) - 1;
        } catch (NumberFormatException ex) {
            return "";
        }

        LeaderboardHandler lb = plugin.getLeaderboardHandler();
        if (lb == null) {
            return "";
        }

        LeaderboardHandler.Entry entry = lb.get(category, index);

        if (entry == null) {
            plugin.getLogger().warning(
                    "[Leaderboard] Missing entry for category '" +
                            category + "' at index " + index
            );

            return suffix.equals("value") ? "0" : "N/A";
        }

        return suffix.equals("value")
                ? String.valueOf(entry.getValue())
                : entry.getName();
    }

    private String formatKdr(int k, int d) {
        if (d == 0) return String.valueOf(k);
        return String.format("%.2f", (double) k / (double) d);
    }
}