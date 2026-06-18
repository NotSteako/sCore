package kami.gg.souppvp.tasks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bson.Document;
import org.bukkit.entity.Player;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;

import java.util.ArrayList;
import java.util.List;

public class SoupPvPPlaceholderExpansion extends PlaceholderExpansion {

    private final SoupPvP plugin;

    public SoupPvPPlaceholderExpansion(SoupPvP plugin) {
        this.plugin = plugin;
    }

    @Override public boolean canRegister() { return true; }
    @Override public String getAuthor()     { return "Strauber"; }
    @Override public String getIdentifier() { return "souppvp"; }
    @Override public String getVersion()    { return "1.0"; }
    @Override public boolean persist()      { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        Profile profile = null;
        if (player != null) {
            profile = plugin.getProfilesHandler().getProfileByUUID(player.getUniqueId());
        }

        switch (identifier) {
            case "tierlevel": return profile == null ? "0" : String.valueOf(profile.getTier().getTierLevel());
            case "kits":      return profile == null ? "0" : String.valueOf(profile.getUnlockedKits().size());
            case "kills":     return profile == null ? "0" : String.valueOf(profile.getKills());
            case "deaths":    return profile == null ? "0" : String.valueOf(profile.getDeaths());
            default:          return handleLeaderboard(identifier);
        }
    }

    /**
     * Handles: leaderboard_<category>_<position>_<name|value>
     * Categories: kills | deaths | kd | killstreak | credits
     */
    private String handleLeaderboard(String identifier) {
        if (!identifier.startsWith("leaderboard_")) return null;

        // identifier example: leaderboard_kills_1_name
        String[] parts = identifier.split("_");
        if (parts.length < 4) return null;

        String category = parts[1].toLowerCase();
        String field    = parts[3].toLowerCase(); // name | value

        int position;
        try {
            position = Integer.parseInt(parts[2]); // 1-based
        } catch (NumberFormatException e) {
            return null;
        }

        // kd sorts by kills, then computes the ratio
        String sortField = mongoSortField(category);
        if (sortField == null) return null;

        List<Document> docs = plugin.getProfilesHandler()
                .getMongoCollection()
                .find()
                .sort(new Document(sortField, -1))
                .skip(position - 1)
                .limit(1)
                .into(new ArrayList<>());

        if (docs.isEmpty()) {
            return field.equals("value") ? "0" : "N/A";
        }

        Document doc = docs.get(0);

        if (field.equals("name")) {
            String name = doc.getString("username");
            return name != null ? name : "N/A";
        }

        if (field.equals("value")) {
            // K/D ratio needs special handling
            if (category.equals("kd")) {
                int k = doc.getInteger("kills", 0);
                int d = doc.getInteger("deaths", 0);
                return d == 0 ? String.valueOf(k) : String.format("%.2f", (double) k / d);
            }
            Object val = doc.get(sortField);
            return val != null ? String.valueOf(val) : "0";
        }

        return null;
    }

    /**
     * Maps category name -> MongoDB field to sort by
     * Field names match exactly what Profile#saveProfile() writes
     */
    private String mongoSortField(String category) {
        switch (category) {
            case "kills":       return "kills";
            case "deaths":      return "deaths";
            case "kd":          return "kills";            // sort by kills, ratio computed above
            case "killstreak":  return "highestKillstreak";
            case "credits":     return "credits";
            default:            return null;
        }
    }
}