package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.refinedev.phoenix.Phoenix;
import xyz.refinedev.phoenix.profile.IProfile;
import xyz.refinedev.phoenix.rank.IRank;

import java.util.ArrayList;
import java.util.List;

public class SoupHoloCommand {

    private static final String[] HOLO_NAMES  = { "SoupKills", "SoupDeaths", "SoupKD", "SoupKillstreak", "SoupCredits" };
    private static final String[] HOLO_TITLES = {
            "&c&l✦ Top Kills ✦",
            "&4&l✦ Top Deaths ✦",
            "&e&l✦ Top K/D ✦",
            "&6&l✦ Top Killstreaks ✦",
            "&b&l✦ Top Credits ✦"
    };
    private static final String[] CATEGORIES  = { "kills", "deaths", "kd", "killstreak", "credits" };

    private static final String[] POSITIONS = {
            "&6&l#1",  "&e&l#2",  "&a&l#3",  "&7&l#4",  "&7&l#5",
            "&7&l#6",  "&7&l#7",  "&7&l#8",  "&7&l#9",  "&7&l#10"
    };

    private final SoupPvP plugin = SoupPvP.getInstance();
    private final BukkitRunnable[] refreshTasks = new BukkitRunnable[5];

    @Command(
            name  = "soupholo",
            desc  = "Manage leaderboard holograms",
            usage = "/soupholo <create|stop|move> <kills|deaths|kd|killstreak|credits|all>"
    )
    public void execute(@Sender CommandSender sender, String action, String target) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cPlayers only."));
            return;
        }
        Player player = (Player) sender;

        switch (action.toLowerCase()) {

            case "create": {
                int[] indices = resolveIndices(target, player);
                if (indices == null) return;
                for (int idx : indices) {
                    stopHolo(idx);
                    Hologram old = DHAPI.getHologram(HOLO_NAMES[idx]);
                    if (old != null) DHAPI.removeHologram(HOLO_NAMES[idx]);
                    DHAPI.createHologram(HOLO_NAMES[idx], player.getLocation().add(0, 2.5, 0), true);
                    startRefreshing(idx);
                    player.sendMessage(CC.translate("&aCreated &e" + HOLO_NAMES[idx] + " &aat your location."));
                }
                break;
            }

            case "stop": {
                int[] indices = resolveIndices(target, player);
                if (indices == null) return;
                for (int idx : indices) {
                    stopHolo(idx);
                    Hologram h = DHAPI.getHologram(HOLO_NAMES[idx]);
                    if (h != null) DHAPI.removeHologram(HOLO_NAMES[idx]);
                    player.sendMessage(CC.translate("&cRemoved &e" + HOLO_NAMES[idx] + "&c."));
                }
                break;
            }

            case "move": {
                int[] indices = resolveIndices(target, player);
                if (indices == null) return;
                for (int idx : indices) {
                    Hologram h = DHAPI.getHologram(HOLO_NAMES[idx]);
                    if (h == null) {
                        player.sendMessage(CC.translate("&c" + HOLO_NAMES[idx] + " doesn't exist. Use /soupholo create first."));
                        continue;
                    }
                    DHAPI.moveHologram(h, player.getLocation().subtract(0, 1, 0));
                    player.sendMessage(CC.translate("&aMoved &e" + HOLO_NAMES[idx] + " &ato your location."));
                }
                break;
            }

            default:
                player.sendMessage(CC.translate("&cUsage: /soupholo <create|stop|move> <kills|deaths|kd|killstreak|credits|all>"));
        }
    }

    private void startRefreshing(int idx) {
        updateLines(idx);
        refreshTasks[idx] = new BukkitRunnable() {
            @Override
            public void run() {
                if (DHAPI.getHologram(HOLO_NAMES[idx]) == null) { cancel(); return; }
                updateLines(idx);
            }
        };
        refreshTasks[idx].runTaskTimer(plugin, 20L * 30, 20L * 30); // every 30 seconds
    }

    private void updateLines(int idx) {
        Hologram holo = DHAPI.getHologram(HOLO_NAMES[idx]);
        if (holo == null) return;

        String category = CATEGORIES[idx];
        List<String> lines = new ArrayList<>();

        lines.add("&b&l&nSoupPvP Leaderboards");
        lines.add(HOLO_TITLES[idx]);
        lines.add("&f");

        for (int i = 1; i <= 10; i++) {
            String nameKey  = "%souppvp_leaderboard_" + category + "_" + i + "_name%";
            String valueKey = "%souppvp_leaderboard_" + category + "_" + i + "_value%";

            String playerName = PlaceholderAPI.setPlaceholders((Player) null, nameKey);
            String value      = PlaceholderAPI.setPlaceholders((Player) null, valueKey);

            boolean validPlayer = playerName != null && !playerName.isEmpty() && !playerName.equals(nameKey);
            boolean validValue  = value      != null && !value.isEmpty()      && !value.equals(valueKey);

            if (!validPlayer) playerName = "N/A";
            if (!validValue)  value      = "0";

            String coloredName;
            if (validPlayer && !playerName.equals("N/A")) {
                coloredName = resolveColoredName(playerName);
            } else {
                coloredName = "&7N/A";
            }

            String line = POSITIONS[i - 1] + " &8» " + coloredName + " &7- &b" + formatValue(category, value);
            lines.add(line);
        }

        lines.add("&f");
        DHAPI.setHologramLines(holo, lines);
    }

    /**
     * Returns the player name with their rank colour prepended — works for
     * offline players too because Phoenix loads profiles straight from Mongo.
     *
     * Resolution order:
     *   1) Phoenix profile by name (offline-safe)
     *   2) Phoenix profile by UUID via OfflinePlayer cache
     *   3) PlaceholderAPI %phoenix_player_rank_color% (online only)
     *   4) Plain white as a last resort
     */
    private String resolveColoredName(String playerName) {
        // 1) Phoenix by name — handles offline players directly.
        try {
            IProfile profile = Phoenix.getInstance().getProfileHandler().getProfileFromName(playerName);
            if (profile == null) {
                profile = Phoenix.getInstance().getProfileHandler().getProfileFromCapitalizedName(playerName);
            }
            String colored = extractColoredName(profile, playerName);
            if (colored != null) return colored;
        } catch (Throwable ignored) {}

        // 2) Phoenix by UUID via OfflinePlayer (cached → no Mojang blocking call).
        try {
            OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(playerName);
            if (op != null && op.getUniqueId() != null) {
                IProfile profile = Phoenix.getInstance().getProfileHandler().getProfile(op.getUniqueId());
                String colored = extractColoredName(profile, playerName);
                if (colored != null) return colored;
            }
        } catch (Throwable ignored) {}

        // 3) PlaceholderAPI fallback (works only when player is online).
        try {
            OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(playerName);
            if (op != null) {
                String phoenixColor = PlaceholderAPI.setPlaceholders(op, "%phoenix_player_rank_color%");
                if (phoenixColor != null && !phoenixColor.isEmpty()
                        && !phoenixColor.equals("%phoenix_player_rank_color%")) {
                    return phoenixColor + playerName;
                }
            }
        } catch (Throwable ignored) {}

        // 4) Plain white.
        return "&f" + playerName;
    }

    /**
     * Best-effort extraction of "&xPlayerName" from a Phoenix profile.
     * Prefers getNameWithColor (already a complete coloured name), then
     * falls back to the highest rank's legacy colour + the supplied name.
     */
    private String extractColoredName(IProfile profile, String fallbackName) {
        if (profile == null) return null;
        try {
            String nameWithColor = profile.getNameWithColor();
            if (nameWithColor != null && !nameWithColor.isEmpty()) return nameWithColor;
        } catch (Throwable ignored) {}
        try {
            IRank rank = profile.getHighestRank();
            if (rank != null) {
                String c = rank.getColorLegacy();
                if (c != null && !c.isEmpty()) return c + fallbackName;
            }
        } catch (Throwable ignored) {}
        try {
            String legacy = profile.getNameColor();
            if (legacy != null && !legacy.isEmpty()) return legacy + fallbackName;
        } catch (Throwable ignored) {}
        return null;
    }

    private String formatValue(String category, String raw) {
        switch (category) {
            case "kd":
                return raw;
            case "credits":
                try {
                    long v = Long.parseLong(raw);
                    if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
                    if (v >= 1_000)     return String.format("%.1fK", v / 1_000.0);
                } catch (NumberFormatException ignored) {}
                return raw;
            default:
                return raw;
        }
    }

    public void restoreRefreshTasks() {
        for (int idx = 0; idx < HOLO_NAMES.length; idx++) {
            if (DHAPI.getHologram(HOLO_NAMES[idx]) != null) {
                stopHolo(idx); // safety
                startRefreshing(idx);
                Bukkit.getLogger().info("[SoupPvP] Restored hologram refresh task for " + HOLO_NAMES[idx]);
            }
        }
    }

    private void stopHolo(int idx) {
        if (refreshTasks[idx] != null) {
            try { refreshTasks[idx].cancel(); } catch (IllegalStateException ignored) {}
            refreshTasks[idx] = null;
        }
    }

    private int[] resolveIndices(String target, Player player) {
        switch (target.toLowerCase()) {
            case "kills":       return new int[]{ 0 };
            case "deaths":      return new int[]{ 1 };
            case "kd":          return new int[]{ 2 };
            case "killstreak":  return new int[]{ 3 };
            case "credits":     return new int[]{ 4 };
            case "all":         return new int[]{ 0, 1, 2, 3, 4 };
            default:
                player.sendMessage(CC.translate("&cUnknown target. Use: kills | deaths | kd | killstreak | credits | all"));
                return null;
        }
    }
}