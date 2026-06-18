package kami.gg.souppvp.guild.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import kami.gg.souppvp.guild.Guild;
import lombok.Getter;
import org.bson.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {

    @Getter
    private final MongoCollection<Document> collection;

    // Keyed by guild name (lower-case for lookup, display name preserved in Guild)
    private final Map<String, Guild> guilds = new HashMap<>();

    public GuildManager(MongoDatabase database) {
        this.collection = database.getCollection("Guilds");
        loadAll();
    }

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    private void loadAll() {
        for (Document doc : collection.find()) {
            Guild guild = Guild.fromDocument(doc);
            guilds.put(guild.getName().toLowerCase(), guild);
        }
    }

    // -------------------------------------------------------------------------
    // CRUD helpers
    // -------------------------------------------------------------------------

    public Guild getGuild(String name) {
        return guilds.get(name.toLowerCase());
    }

    public Guild getGuildByMember(UUID uuid) {
        for (Guild guild : guilds.values()) {
            if (guild.getMembers().contains(uuid)) {
                return guild;
            }
        }
        return null;
    }

    public boolean guildExists(String name) {
        return guilds.containsKey(name.toLowerCase());
    }

    public void createGuild(String name, UUID leader) {
        Guild guild = new Guild(name, leader);
        guilds.put(name.toLowerCase(), guild);
        guild.save(collection);
    }

    public void disbandGuild(String name) {
        Guild guild = guilds.remove(name.toLowerCase());
        if (guild != null) {
            guild.delete(collection);
        }
    }

    public void saveGuild(Guild guild) {
        guild.save(collection);
    }

    public Collection<Guild> getAllGuilds() {
        return guilds.values();
    }
}