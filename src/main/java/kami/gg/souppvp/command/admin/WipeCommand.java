package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.annotation.Text;
import com.mongodb.client.model.Filters;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /wipe <player> <reason>        — works on online AND offline players (by name)
 * /wipebyuuid <uuid> <reason>    — wipe by raw UUID string
 *
 * Register in your main class:
 *   drink.register(new WipeCommand(), "wipe");
 *   drink.register(new WipeCommand(), "wipebyuuid");
 *   drink.registerCommands();
 *
 * plugin.yml:
 *   wipe:
 *     description: Wipe a player's data (online or offline).
 *     usage: /wipe <player> <reason>
 *   wipebyuuid:
 *     description: Wipe a player's data by UUID.
 *     usage: /wipebyuuid <uuid> <reason>
 */
public class WipeCommand {

    private final SoupPvP plugin = SoupPvP.getInstance();

    // =========================================================================
    //  /wipe — by player name (online or offline)
    // =========================================================================

    @Command(name = "", desc = "Wipe a player's data by name.", usage = "<player> <reason>")
    @Require("souppvp.wipe")
    public void wipeRoot(@Sender CommandSender sender) {
        sender.sendMessage(CC.translate("&cUsage: &f/wipe <player> <reason>"));
    }

    /**
     * Accepts a plain String name so Drink doesn't reject offline players.
     * We resolve the UUID ourselves via OfflinePlayer (Paper's user cache,
     * no blocking HTTP call if the player has joined before).
     */
    @Command(name = "name", desc = "Wipe a player's data by name.", usage = "<player> <reason>")
    @Require("souppvp.wipe")
    public void wipeByName(@Sender CommandSender sender, String playerName, @Text String reason) {

        // Resolve offline player — Paper looks this up from the local user-cache
        // so it is safe to call on the main thread for anyone who has joined before.
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            sender.sendMessage(CC.translate(
                    "&cNo player named &f" + playerName + " &chas ever joined this server."));
            return;
        }

        String targetUUID = offlinePlayer.getUniqueId().toString();
        String targetName = offlinePlayer.getName() != null ? offlinePlayer.getName() : playerName;

        performWipe(sender, targetUUID, targetName, reason);
    }

    // =========================================================================
    //  /wipebyuuid — by raw UUID string
    // =========================================================================

    @Command(name = "", desc = "Wipe a player's data by UUID.", usage = "<uuid> <reason>")
    @Require("souppvp.wipe")
    public void wipeByUuidRoot(@Sender CommandSender sender) {
        sender.sendMessage(CC.translate("&cUsage: &f/wipebyuuid <uuid> <reason>"));
    }

    @Command(name = "uuid", desc = "Wipe a player's data by UUID.", usage = "<uuid> <reason>")
    @Require("souppvp.wipe")
    public void wipeByUuid(@Sender CommandSender sender, String rawUuid, @Text String reason) {

        UUID targetUUID;
        try {
            targetUUID = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(CC.translate(
                    "&cInvalid UUID format. Expected: &fxxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
            return;
        }

        // Try to get a display name from cache; fall back to the UUID string itself
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
        String targetName = offlinePlayer.getName() != null ? offlinePlayer.getName() : rawUuid;

        performWipe(sender, targetUUID.toString(), targetName, reason);
    }

    // =========================================================================
    //  Shared wipe logic
    // =========================================================================

    private void performWipe(CommandSender sender, String targetUUID, String targetName, String reason) {

        // ── 1. Evict from in-memory profile cache (if loaded) ─────────────────
        try {
            UUID uuid = UUID.fromString(targetUUID);
            plugin.getProfilesHandler().getProfiles().remove(uuid);
        } catch (Exception ignored) { /* UUID already validated before reaching here */ }

        // ── 2. Kick the player if they are currently online ───────────────────
        Player online = Bukkit.getPlayer(targetName);
        if (online != null && online.isOnline()) {
            online.kickPlayer(CC.translate(
                    "&cYour data has been wiped by a staff member.\n" +
                            "&7Reason: &f" + reason));
        }

        // ── 3. Delete from MongoDB asynchronously ─────────────────────────────
        // Collection: SoupPvP → Profiles  |  field: "uuid" (string)
        final String name = targetName;
        final String uuid = targetUUID;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long deleted = plugin.getMongoDatabase()
                        .getCollection("Profiles")
                        .deleteOne(Filters.eq("uuid", uuid))
                        .getDeletedCount();

                Bukkit.getScheduler().runTask(plugin, () -> {

                    // ── 4. Feedback to sender ─────────────────────────────────
                    if (deleted > 0) {
                        sender.sendMessage(CC.translate(
                                "&aSuccessfully wiped &f" + name +
                                        "&a's profile from MongoDB."));
                    } else {
                        sender.sendMessage(CC.translate(
                                "&eNo MongoDB document found for &f" + name +
                                        " &e(&7" + uuid + "&e) — in-memory cache cleared anyway."));
                    }

                    // ── 5. Server-wide broadcast ──────────────────────────────
                    Bukkit.broadcastMessage(CC.translate(
                            "&c&l[WIPE] &f" + name +
                                    " &chas had their data wiped by &f" + sender.getName() +
                                    "&c. &7Reason: &f" + reason));

                    // ── 6. Console log ────────────────────────────────────────
                    plugin.getLogger().info("[Wipe] " + sender.getName() +
                            " wiped " + name + " (" + uuid + ")." +
                            " Reason: " + reason +
                            " | MongoDB docs removed: " + deleted);
                });

            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(CC.translate(
                                "&cFailed to delete &f" + name +
                                        " &cfrom MongoDB — check console.")));
                plugin.getLogger().severe("[Wipe] MongoDB deletion failed for " +
                        name + " (" + uuid + "): " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}