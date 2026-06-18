package kami.gg.souppvp.handlers;

import com.mongodb.client.MongoCollection;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileListeners;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class ProfilesHandler {

    private MongoCollection<Document> mongoCollection = SoupPvP.getInstance().getMongoDatabase().getCollection("Profiles");

    private List<Profile> profiles;

    public ProfilesHandler() {
        profiles = new ArrayList<>();
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new ProfileListeners(), SoupPvP.getInstance());
    }

    public Profile getProfileByName(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;

        // 1. Check in-memory profiles first (covers both online and recently-loaded offline)
        for (Profile profile : profiles) {
            if (profile.getUsername() != null && profile.getUsername().equalsIgnoreCase(playerName)) {
                return profile;
            }
        }

        // 2. Try online player
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return getProfileByUUID(onlinePlayer.getUniqueId());
        }

        // 3. Try offline player (has played before = UUID is known and trustworthy)
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName);
        if (offlinePlayer != null && offlinePlayer.hasPlayedBefore() && offlinePlayer.getUniqueId() != null) {
            for (Profile profile : profiles) {
                if (profile.getUuid().equals(offlinePlayer.getUniqueId())) {
                    return profile;
                }
            }
            // Not in memory — create a stub from their known UUID (don't use name-only constructor)
            return new Profile(offlinePlayer.getUniqueId());
        }

        // 4. Unknown player — do NOT attempt to create a profile with no UUID
        return null;
    }

    public Profile getProfileByUUID(UUID uuid) {
        if (uuid == null) return null;

        for (Profile profile : profiles) {
            if (profile.getUuid().equals(uuid)) {
                return profile;
            }
        }
        return null;
    }

    public void saveProfiles() {
        for (Profile profile : SoupPvP.getInstance().getProfilesHandler().getProfiles()) {
            profile.saveProfile();
        }
    }

}