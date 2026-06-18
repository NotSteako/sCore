package kami.gg.souppvp.handlers;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildStats;
import kami.gg.souppvp.profile.Profile;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import java.util.*;

@Getter
public class GuildsHandler {

    private final MongoCollection<Document> mongoCollection =
            SoupPvP.getInstance().getMongoDatabase().getCollection("Guilds");

    private final Map<String, Guild> guildsByLowerName = new HashMap<>();
    private final List<String> blockedNames = new ArrayList<>();

    public GuildsHandler() {
        loadBlockedNames();
        loadAll();
    }

    private void loadBlockedNames() {
        blockedNames.clear();
        List<String> configured = SoupPvP.getInstance().getConfig().getStringList("GUILDS.BLOCKED-NAMES");
        if (configured != null) {
            for (String s : configured) {
                if (s != null && !s.isEmpty()) blockedNames.add(s.toLowerCase(Locale.ROOT));
            }
        }
    }

    public boolean isBlocked(String name) {
        return name == null || blockedNames.contains(name.toLowerCase(Locale.ROOT));
    }

    private void loadAll() {
        for (Document doc : mongoCollection.find()) {
            Guild g = fromDocument(doc);
            if (g != null && g.getName() != null) {
                guildsByLowerName.put(g.getName().toLowerCase(Locale.ROOT), g);
            }
        }
    }

    public void save(Guild guild) {
        if (guild == null || guild.getName() == null) return;
        mongoCollection.replaceOne(
                Filters.eq("nameLower", guild.getName().toLowerCase(Locale.ROOT)),
                toDocument(guild),
                new ReplaceOptions().upsert(true));
    }

    public void saveAll() { for (Guild g : guildsByLowerName.values()) save(g); }

    private void deleteFromMongo(String name) {
        if (name == null) return;
        mongoCollection.deleteOne(Filters.eq("nameLower", name.toLowerCase(Locale.ROOT)));
    }

    private Document toDocument(Guild g) {
        Document doc = new Document();
        doc.append("name", g.getName());
        doc.append("nameLower", g.getName().toLowerCase(Locale.ROOT));
        doc.append("leader", g.getLeader() == null ? null : g.getLeader().toString());
        List<String> members = new ArrayList<>();
        for (UUID u : g.getMembers()) members.add(u.toString());
        doc.append("members", members);
        List<String> invites = new ArrayList<>();
        for (UUID u : g.getPendingInvites()) invites.add(u.toString());
        doc.append("pendingInvites", invites);
        doc.append("tag", g.getTag());
        return doc;
    }

    @SuppressWarnings("unchecked")
    private Guild fromDocument(Document doc) {
        try {
            Guild g = new Guild();
            g.setName(doc.getString("name"));
            String leader = doc.getString("leader");
            if (leader != null) g.setLeader(UUID.fromString(leader));
            List<String> members = (List<String>) doc.get("members");
            List<UUID> memberUUIDs = new ArrayList<>();
            if (members != null) for (String s : members) { try { memberUUIDs.add(UUID.fromString(s)); } catch (Exception ignored) {} }
            g.setMembers(memberUUIDs);
            List<String> invites = (List<String>) doc.get("pendingInvites");
            List<UUID> inviteUUIDs = new ArrayList<>();
            if (invites != null) for (String s : invites) { try { inviteUUIDs.add(UUID.fromString(s)); } catch (Exception ignored) {} }
            g.setPendingInvites(inviteUUIDs);
            String tag = doc.getString("tag");
            g.setTag(tag == null ? "&7" : tag);
            return g;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public Guild getByName(String name) {
        if (name == null) return null;
        return guildsByLowerName.get(name.toLowerCase(Locale.ROOT));
    }

    public Guild getByPlayer(UUID uuid) {
        if (uuid == null) return null;
        for (Guild g : guildsByLowerName.values()) if (g.isMember(uuid)) return g;
        return null;
    }

    public Guild create(String name, UUID leader) {
        Guild g = new Guild(name, leader);
        guildsByLowerName.put(name.toLowerCase(Locale.ROOT), g);
        save(g);
        return g;
    }

    public boolean isTaken(String name) {
        return name != null && guildsByLowerName.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public void disband(Guild g) {
        if (g == null || g.getName() == null) return;
        // Clear guildName on every member's profile first (online + offline).
        for (UUID uuid : new ArrayList<>(g.getMembers())) {
            setProfileGuild(uuid, null);
        }
        guildsByLowerName.remove(g.getName().toLowerCase(Locale.ROOT));
        deleteFromMongo(g.getName());
    }

    /* ---------------------------------------------------------------- *
     *  Profile sync — keeps Profile.guildName in lock-step with guild
     *  membership so offline players can still be aggregated.
     * ---------------------------------------------------------------- */

    /**
     * Sets a player's {@code Profile.guildName} both in memory (if loaded) and
     * directly in the Mongo Profiles collection (so it sticks for offline
     * players too). Pass {@code null} to clear.
     */
    public void setProfileGuild(UUID uuid, String guildName) {
        if (uuid == null) return;
        try {
            Profile p = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(uuid);
            if (p != null) p.setGuildName(guildName);
        } catch (Throwable ignored) {}

        try {
            MongoCollection<Document> profiles =
                    SoupPvP.getInstance().getProfilesHandler().getMongoCollection();
            profiles.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    new Document("$set", new Document("guildName", guildName))
            );
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /* ---------------------------------------------------------------- *
     *  Aggregated stats — single Mongo round-trip across all members.
     * ---------------------------------------------------------------- */

    public GuildStats aggregateStats(Guild guild) {
        GuildStats stats = new GuildStats();
        if (guild == null || guild.getMembers() == null || guild.getMembers().isEmpty()) {
            return stats;
        }
        stats.memberCount = guild.getMembers().size();

        List<String> uuids = new ArrayList<>();
        for (UUID u : guild.getMembers()) uuids.add(u.toString());

        try {
            MongoCollection<Document> profiles =
                    SoupPvP.getInstance().getProfilesHandler().getMongoCollection();

            Document group = new Document("_id", null)
                    .append("totalKills",         new Document("$sum", "$kills"))
                    .append("totalDeaths",        new Document("$sum", "$deaths"))
                    .append("totalCredits",       new Document("$sum", "$credits"))
                    .append("totalExperiences",   new Document("$sum", "$experiences"))
                    .append("totalEventsWon",     new Document("$sum", "$eventsStatistics.eventsWon"))
                    .append("highestKillstreak",  new Document("$max", "$highestKillstreak"));

            AggregateIterable<Document> agg = profiles.aggregate(Arrays.asList(
                    new Document("$match", new Document("uuid", new Document("$in", uuids))),
                    new Document("$group", group)
            ));
            Document result = agg.first();
            if (result != null) {
                stats.totalKills        = safeInt(result, "totalKills");
                stats.totalDeaths       = safeInt(result, "totalDeaths");
                stats.totalCredits      = safeInt(result, "totalCredits");
                stats.totalExperiences  = safeInt(result, "totalExperiences");
                stats.totalEventsWon    = safeInt(result, "totalEventsWon");
                stats.highestKillstreak = safeInt(result, "highestKillstreak");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return stats;
    }

    private static int safeInt(Document doc, String key) {
        Object v = doc.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }
}