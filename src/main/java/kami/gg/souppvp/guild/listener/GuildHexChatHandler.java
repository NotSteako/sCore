package kami.gg.souppvp.guild.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.guild.menu.GuildTagMenu;
import kami.gg.souppvp.listener.LunarClientListener;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Chat-based custom hex colour entry flow.
 *
 * Clicking the "Custom" glass block closes the menu and calls
 * {@link #beginInput(Player, Guild)}. The player's next chat message is
 * intercepted, validated as a hex code, and either applied to the guild tag
 * or cancelled. Replaces the previous anvil-based flow.
 */
public class GuildHexChatHandler implements Listener {

    private static final Map<UUID, Guild> WAITING = new HashMap<>();
    private static final Pattern HEX = Pattern.compile("^#?[0-9A-Fa-f]{6}$");

    public static void beginInput(Player player, Guild guild) {
        WAITING.put(player.getUniqueId(), guild);
        player.sendMessage(GuildText.translate("&8&m----------------------------------------"));
        player.sendMessage(GuildText.translate("&dGuild tag — &fcustom hex colour"));
        player.sendMessage("");
        player.sendMessage(GuildText.translate("&7Type the &fhex code &7of your choice in chat."));
        // The literal "#ff66cc" must NOT go through the hex translator (which
        // would convert it into an invisible chat-colour code). Concatenate
        // the rendered prefix with the raw literal so the player actually
        // sees the example.
        player.sendMessage(GuildText.translate("&7Example: &f") + "#ff66cc");
        player.sendMessage(GuildText.translate("&7Type &ccancel &7to abort without changing anything."));
        player.sendMessage(GuildText.translate("&8&m----------------------------------------"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.4F);
    }

    public static boolean isWaiting(UUID uuid) {
        return WAITING.containsKey(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = WAITING.get(player.getUniqueId());
        if (guild == null) return;

        // Swallow the message — it isn't real chat.
        event.setCancelled(true);
        WAITING.remove(player.getUniqueId());

        String raw = event.getMessage() == null ? "" : event.getMessage().trim();

        if (raw.equalsIgnoreCase("cancel") || raw.equalsIgnoreCase("exit") || raw.equalsIgnoreCase("quit")) {
            player.sendMessage(GuildText.translate("&7Cancelled. Tag colour unchanged."));
            reopenMenuLater(player);
            return;
        }

        String normalised = normalise(raw);
        if (normalised == null) {
            // Same trick as in beginInput: the literal "#ff66cc" must not be
            // hex-translated.
            player.sendMessage(GuildText.translate(
                    "&c\"" + raw + "\" isn't a valid hex code. Use a 6-digit hex, e.g. &f")
                    + "#ff66cc"
                    + GuildText.translate("&c."));
            player.sendMessage(GuildText.translate(
                    "&7Run &e/guild tag &7and click the &fCustom Hex Colour &7block to try again."));
            return;
        }

        // Re-fetch in case the player's guild changed between menu open and chat input.
        Guild current = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (current == null || !current.getName().equalsIgnoreCase(guild.getName())) {
            player.sendMessage(GuildText.translate("&cYou are no longer in that guild."));
            return;
        }
        if (!current.isLeader(player.getUniqueId())) {
            player.sendMessage(GuildText.translate("&cOnly the guild leader can change the tag."));
            return;
        }

        current.setTag(normalised);
        SoupPvP.getInstance().getGuildsHandler().save(current);

        player.sendMessage(GuildText.translate(
                "&aGuild tag colour set to " + "&7[&r"+ normalised + current.getName() + "&7]&a."));

        // Sound + nametag refresh must hop to the main thread (chat event is async).
        Bukkit.getScheduler().runTask(SoupPvP.getInstance(), () -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1F, 1.6F);
            LunarClientListener.updateNametag(player);
        });

        reopenMenuLater(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        WAITING.remove(event.getPlayer().getUniqueId());
    }

    private static void reopenMenuLater(Player player) {
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(),
                () -> { if (player.isOnline()) new GuildTagMenu().openMenu(player); }, 2L);
    }

    private static String normalise(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (!s.startsWith("#")) s = "#" + s;
        if (!HEX.matcher(s).matches()) return null;
        return s.toLowerCase();
    }
}