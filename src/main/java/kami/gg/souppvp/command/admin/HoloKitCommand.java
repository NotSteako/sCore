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

public class HoloKitCommand {

    // ── Kit names must match exactly what your LeaderboardHandler uses as keys ──
    private static final String[] KIT_NAMES = {
            "Spiderman", "Jett", "Reaper"
            // Add / remove kits to match your server's kit list
    };

    private static final String[] POSITIONS = {
            "&6&l#1", "&e&l#2", "&a&l#3", "&7&l#4", "&7&l#5",
            "&7&l#6", "&7&l#7", "&7&l#8", "&7&l#9", "&7&l#10"
    };

    private final SoupPvP plugin = SoupPvP.getInstance();

    /** One refresh task per kit slot. */
    private final BukkitRunnable[] refreshTasks = new BukkitRunnable[KIT_NAMES.length];

    // ── Hologram name: "HoloKit_Diamond", "HoloKit_Iron", etc. ──────────────────
    private static String holoName(int idx) {
        return "HoloKit_" + KIT_NAMES[idx];
    }

    @Command(
            name  = "holokit",
            desc  = "Manage per-kit kill leaderboard holograms",
            usage = "/holokit <create|stop|move> <kitname|all>"
    )
    public void execute(@Sender CommandSender sender, String action, String target) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cPlayers only."));
            return;
        }
        Player player = (Player) sender;

        int[] indices = resolveIndices(target, player);
        if (indices == null) return;

        switch (action.toLowerCase()) {

            case "create": {
                for (int idx : indices) {
                    stopHolo(idx);
                    Hologram old = DHAPI.getHologram(holoName(idx));
                    if (old != null) DHAPI.removeHologram(holoName(idx));
                    DHAPI.createHologram(holoName(idx), player.getLocation().add(0, 2.5, 0), true);
                    startRefreshing(idx);
                    player.sendMessage(CC.translate("&aCreated &e" + holoName(idx) + " &aat your location."));
                }
                break;
            }

            case "stop": {
                for (int idx : indices) {
                    stopHolo(idx);
                    Hologram h = DHAPI.getHologram(holoName(idx));
                    if (h != null) DHAPI.removeHologram(holoName(idx));
                    player.sendMessage(CC.translate("&cRemoved &e" + holoName(idx) + "&c."));
                }
                break;
            }

            case "move": {
                for (int idx : indices) {
                    Hologram h = DHAPI.getHologram(holoName(idx));
                    if (h == null) {
                        player.sendMessage(CC.translate("&c" + holoName(idx) + " doesn't exist. Use /holokit create first."));
                        continue;
                    }
                    DHAPI.moveHologram(h, player.getLocation().subtract(0, 1, 0));
                    player.sendMessage(CC.translate("&aMoved &e" + holoName(idx) + " &ato your location."));
                }
                break;
            }

            default:
                player.sendMessage(CC.translate("&cUsage: /holokit <create|stop|move> <kitname|all>"));
        }
    }

    // ── Refresh ──────────────────────────────────────────────────────────────────

    private void startRefreshing(int idx) {
        updateLines(idx);
        refreshTasks[idx] = new BukkitRunnable() {
            @Override
            public void run() {
                if (DHAPI.getHologram(holoName(idx)) == null) { cancel(); return; }
                updateLines(idx);
            }
        };
        refreshTasks[idx].runTaskTimer(plugin, 20L * 30, 20L * 30); // every 30 s
    }

    private void updateLines(int idx) {
        Hologram holo = DHAPI.getHologram(holoName(idx));
        if (holo == null) return;

        String kit = KIT_NAMES[idx].toLowerCase(); // placeholder key is lower-case

        List<String> lines = new ArrayList<>();
        lines.add("&b&l&nSoupPvP Leaderboards");
        lines.add("&c&l✦ Top " + KIT_NAMES[idx] + " Kills ✦");
        lines.add("&f");

        for (int i = 1; i <= 10; i++) {
            // Placeholders: %souppvp_leaderboard_kit_diamond_1_name%  /  _value%
            String nameKey  = "%souppvp_leaderboard_kit_" + kit + "_" + i + "_name%";
            String valueKey = "%souppvp_leaderboard_kit_" + kit + "_" + i + "_value%";

            String playerName = PlaceholderAPI.setPlaceholders((Player) null, nameKey);
            String value      = PlaceholderAPI.setPlaceholders((Player) null, valueKey);

            boolean validPlayer = playerName != null && !playerName.isEmpty() && !playerName.equals(nameKey);
            boolean validValue  = value      != null && !value.isEmpty()      && !value.equals(valueKey);

            if (!validPlayer) playerName = "N/A";
            if (!validValue)  value      = "0";

            String coloredName = validPlayer && !playerName.equals("N/A")
                    ? resolveColoredName(playerName)
                    : "&7N/A";

            lines.add(POSITIONS[i - 1] + " &8» " + coloredName + " &7- &b" + value + " &7kills");
        }

        lines.add("&f");
        DHAPI.setHologramLines(holo, lines);
    }

    // ── Restore on reload (call from your main plugin class) ─────────────────────

    public void restoreRefreshTasks() {
        for (int idx = 0; idx < KIT_NAMES.length; idx++) {
            if (DHAPI.getHologram(holoName(idx)) != null) {
                stopHolo(idx);
                startRefreshing(idx);
                Bukkit.getLogger().info("[SoupPvP] Restored HoloKit refresh task for " + holoName(idx));
            }
        }
    }

    // ── Helpers (identical logic to SoupHoloCommand) ─────────────────────────────

    private String resolveColoredName(String playerName) {
        try {
            IProfile profile = Phoenix.getInstance().getProfileHandler().getProfileFromName(playerName);
            if (profile == null)
                profile = Phoenix.getInstance().getProfileHandler().getProfileFromCapitalizedName(playerName);
            String colored = extractColoredName(profile, playerName);
            if (colored != null) return colored;
        } catch (Throwable ignored) {}

        try {
            OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(playerName);
            if (op != null && op.getUniqueId() != null) {
                IProfile profile = Phoenix.getInstance().getProfileHandler().getProfile(op.getUniqueId());
                String colored = extractColoredName(profile, playerName);
                if (colored != null) return colored;
            }
        } catch (Throwable ignored) {}

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

        return "&f" + playerName;
    }

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

    private void stopHolo(int idx) {
        if (refreshTasks[idx] != null) {
            try { refreshTasks[idx].cancel(); } catch (IllegalStateException ignored) {}
            refreshTasks[idx] = null;
        }
    }

    private int[] resolveIndices(String target, Player player) {
        String t = target.toLowerCase();
        if (t.equals("all")) {
            int[] all = new int[KIT_NAMES.length];
            for (int i = 0; i < all.length; i++) all[i] = i;
            return all;
        }
        for (int i = 0; i < KIT_NAMES.length; i++) {
            if (KIT_NAMES[i].equalsIgnoreCase(target)) return new int[]{ i };
        }
        player.sendMessage(CC.translate(
                "&cUnknown kit. Available: " + String.join(" | ", KIT_NAMES).toLowerCase() + " | all"
        ));
        return null;
    }
}