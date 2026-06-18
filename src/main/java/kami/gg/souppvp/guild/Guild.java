package kami.gg.souppvp.guild;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Guild {

    private String name;
    private UUID leader;
    private List<UUID> members;
    private List<UUID> pendingInvites;
    /** Legacy code "&a"/"&1"/... OR hex "#rrggbb". Default "&7". */
    private String tag;
    private int kills;
    private int deaths;

    public Guild(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.pendingInvites = new ArrayList<>();
        this.tag = "&7";

        this.kills = 0;
        this.deaths = 0;
    }

    public Guild() {
        this.members = new ArrayList<>();
        this.pendingInvites = new ArrayList<>();
        this.tag = "&7";
    }

    /** "&a[GuildName]" or "#ff66cc[GuildName]". */
    /** Returns a translated, ready-to-send colored tag string. */
    public String getColoredTag() {
        String raw;
        if (tag == null || tag.isEmpty()) {
            raw = "&7[&7" + name + "&7]";
        } else {
            raw = "&7[" + tag + name + "&7]";
        }
        return GuildText.translate(raw);
    }

    public boolean isLeader(UUID uuid) {
        return leader != null && leader.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members != null && members.contains(uuid);
    }


    // -------------------------------------------------------------------------
    // Online check — a guild is considered "online" if ANY member is online
    // -------------------------------------------------------------------------

    public boolean isOnline() {
        for (UUID uuid : members) {
            if (Bukkit.getPlayer(uuid) != null) {
                return true;
            }
        }
        return false;
    }

    public int getOnlineCount() {
        int count = 0;
        for (UUID uuid : members) {
            if (Bukkit.getPlayer(uuid) != null) {
                count++;
            }
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // MongoDB serialisation
    // -------------------------------------------------------------------------

    public Document toDocument() {
        List<String> memberStrings = new ArrayList<>();
        for (UUID uuid : members) {
            memberStrings.add(uuid.toString());
        }

        return new Document("name", name)
                .append("leader", leader.toString())
                .append("members", memberStrings)
                .append("kills", kills)
                .append("deaths", deaths);
    }

    public static Guild fromDocument(Document doc) {
        String name = doc.getString("name");
        UUID leader = UUID.fromString(doc.getString("leader"));

        Guild guild = new Guild(name, leader);
        guild.setKills(doc.getInteger("kills", 0));
        guild.setDeaths(doc.getInteger("deaths", 0));

        guild.getMembers().clear();
        List<String> memberStrings = doc.getList("members", String.class);
        if (memberStrings != null) {
            for (String s : memberStrings) {
                guild.getMembers().add(UUID.fromString(s));
            }
        }

        return guild;
    }

    public void save(MongoCollection<Document> collection) {
        collection.replaceOne(
                Filters.eq("name", name),
                toDocument(),
                new ReplaceOptions().upsert(true)
        );
    }

    public void delete(MongoCollection<Document> collection) {
        collection.deleteOne(Filters.eq("name", name));
    }
}