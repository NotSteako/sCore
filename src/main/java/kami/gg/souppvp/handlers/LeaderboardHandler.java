package kami.gg.souppvp.handlers;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class LeaderboardHandler {

    // category -> ordered top entries (size <= TOP_N)
    private final Map<String, List<Entry>> leaderboards = new ConcurrentHashMap<>();

    private static final int TOP_N = 10;
    private static final long REFRESH_TICKS = 20L * 60L; // 60s

    // category -> mongo field name. "wagers.wagersWon" works because Mongo supports dotted paths.
    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();

    // LB KITS
    private static final String[] KIT_NAMES = { "Spiderman", "Jett", "Reaper" };

    static {
        CATEGORIES.put("kills",            "kills");
        CATEGORIES.put("deaths",           "deaths");
        CATEGORIES.put("killstreak",       "highestKillstreak");
        CATEGORIES.put("bounty",           "bounty");
        CATEGORIES.put("credits",          "credits");
        CATEGORIES.put("experiences",      "experiences");
        CATEGORIES.put("eventswon",        "eventsStatistics.eventsWon");
        CATEGORIES.put("wagerswon",        "wagers.wagersWon");
        CATEGORIES.put("wagerslost",       "wagers.wagersLost");
        CATEGORIES.put("totalwagergames",  "wagers.totalWagersGames");

        for (String kit : KIT_NAMES) {
            CATEGORIES.put("kit_" + kit.toLowerCase(), "kitKills." + kit.toLowerCase());
        }
    }

    public Set<String> getCategories() {
        return CATEGORIES.keySet();
    }

    public void refreshAll() {
        MongoCollection<Document> col = SoupPvP.getInstance().getProfilesHandler().getMongoCollection();
        for (Map.Entry<String, String> e : CATEGORIES.entrySet()) {
            String category = e.getKey();
            String field    = e.getValue();

            List<Entry> top = new ArrayList<>(TOP_N);
            try {
                col.find()
                        .sort(Sorts.descending(field))
                        .limit(TOP_N)
                        .forEach((Block<? super Document>) doc -> top.add(toEntry(doc, field)));
            } catch (Exception ex) {
                SoupPvP.getInstance().getLogger().warning(
                        "Leaderboard refresh failed for " + category + ": " + ex.getMessage());
                continue;
            }
            leaderboards.put(category, top);
        }
    }

    private Entry toEntry(Document doc, String field) {
        String name = doc.getString("username");
        if (name == null) name = "Unknown";
        Number raw = readDotted(doc, field);
        long value = raw == null ? 0L : raw.longValue();
        return new Entry(name, value);
    }

    // supports "wagers.wagersWon"
    private Number readDotted(Document root, String path) {
        Object cur = root;
        for (String p : path.split("\\.")) {
            if (!(cur instanceof Document)) return null;
            cur = ((Document) cur).get(p);
        }
        return (cur instanceof Number) ? (Number) cur : null;
    }

    public Entry get(String category, int index) {
        List<Entry> list = leaderboards.get(category);
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    @Getter
    public static final class Entry {
        private final String name;
        private final long value;
        public Entry(String name, long value) { this.name = name; this.value = value; }
    }

    public LeaderboardHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                SoupPvP.getInstance(),
                this::refreshAll,
                20L,
                REFRESH_TICKS
        );
    }

}